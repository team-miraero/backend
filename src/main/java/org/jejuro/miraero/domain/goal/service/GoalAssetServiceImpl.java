package org.jejuro.miraero.domain.goal.service;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.exception.AccountErrorCode;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.dto.response.AutoTransferResponse;
import org.jejuro.miraero.domain.autotransfer.dto.response.WithdrawalAccountResponse;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.domain.GoalType;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalPullFundsResponse;
import org.jejuro.miraero.domain.goal.dto.response.asset.AssetDetailResponse;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetListResponse;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetResponse;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.mydata.service.AccountTransferService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoalAssetServiceImpl implements GoalAssetService {

    private static final String CHECKING = "CHECKING";

    private final GoalAssetMapper goalAssetMapper;
    private final GoalMapper goalMapper;
    private final AccountMapper accountMapper;
    private final AutoTransferMapper autoTransferMapper;
    private final MoneyBoxMapper moneyBoxMapper;
    private final AccountTransferService accountTransferService;



    @Override
    @Transactional
    public void saveGoalAssets(Long userId, Long goalId, List<GoalAssetRequest> assets) {


        Goal goal = goalMapper.findByIdAndUserId(
                userId,
                goalId
        );


        if(goal == null){
            throw new BusinessException(
                    GoalErrorCode.GOAL_NOT_FOUND
            );
        }

        if(assets == null || assets.isEmpty()){
            return;
        }

        validateAssets(userId,goal ,assets);

        validateDuplicateAssets(assets);

        goalAssetMapper.saveAll(goalId, assets);
    }

    @Override
    @Transactional
    public GoalPullFundsResponse pullFunds(Long userId, Long goalId, GoalPullFundsRequest request) {
        Goal goal = goalMapper.findByIdAndUserId(userId, goalId);

        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        Long sourceAccountId = request.getSourceAccountId();
        Long amount = request.getAmount();

        AccountResponse sourceAccount =
                accountMapper.findResponseByIdAndUserId(sourceAccountId, userId);

        if (sourceAccount == null) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        // 이미 다른 목표의 자산으로 쓰이는 계좌는 그 목표를 위한 돈이라 끌어쓸 수 없다
        if (goalAssetMapper.existsByAsset(AssetType.ACCOUNT, sourceAccountId)) {
            throw new BusinessException(GoalErrorCode.PULL_SOURCE_ACCOUNT_LINKED);
        }

        // 조회 잔액은 이미 그 계좌에 딸린 저금통 몫이 빠진, 실제로 끌어쓸 수 있는 값이다
        if (sourceAccount.getBalance() < amount) {
            throw new BusinessException(GoalErrorCode.PULL_INSUFFICIENT_BALANCE);
        }

        GoalAsset target = findPullTarget(goalId);
        Long targetAccountId = resolveTargetAccountId(target);

        // 같은 계좌면 이체가 성립하지 않는다. 외부 이체 API가 거부하기 전에 걸러낸다.
        if (sourceAccountId.equals(targetAccountId)) {
            throw new BusinessException(GoalErrorCode.PULL_SAME_ACCOUNT);
        }

        accountMapper.decreaseBalance(sourceAccountId, userId, amount);

        accountTransferService.transfer(userId, sourceAccountId, targetAccountId, amount);
        accountMapper.increaseBalance(targetAccountId, userId, amount);

        // 저금통이 대상이면 계좌로 옮긴 금액을 저금통 몫으로 다시 묶는다
        if (target.getAssetType() == AssetType.MONEY_BOX) {
            moneyBoxMapper.increaseBalance(target.getAssetId(), amount);
        }

        return GoalPullFundsResponse.builder()
                .pulledAmount(amount)
                .currentAmount(calculateCurrentAmount(userId,goalId))
                .build();
    }

    // 목표 하나는 사실상 저축 자산 하나(예적금 또는 저금통)로 운영되므로 처음 찾은 걸 쓴다
    private GoalAsset findPullTarget(Long goalId) {
        return goalAssetMapper.findByGoalId(goalId).stream()
                .filter(asset -> asset.getAssetType() == AssetType.ACCOUNT
                        || asset.getAssetType() == AssetType.MONEY_BOX)
                .findFirst()
                .orElseThrow(() -> new BusinessException(GoalErrorCode.PULL_TARGET_NOT_SUPPORTED));
    }

    /**
     * 자금이 실제로 들어갈 계좌를 찾는다.
     * 저금통은 자체 계좌가 없으므로 소속 계좌가 입금 대상이 된다.
     */
    private Long resolveTargetAccountId(GoalAsset target) {
        if (target.getAssetType() != AssetType.MONEY_BOX) {
            return target.getAssetId();
        }

        MoneyBox moneyBox = moneyBoxMapper.findById(target.getAssetId());

        if (moneyBox == null) {
            throw new BusinessException(GoalErrorCode.PULL_TARGET_NOT_SUPPORTED);
        }

        return moneyBox.getAccountId();
    }

    private void validateDuplicateAssets(
            List<GoalAssetRequest> assets
    ) {
        for (GoalAssetRequest asset : assets) {
            if (goalAssetMapper.existsByAsset(
                    asset.getAssetType(),
                    asset.getAssetId()
            )) {
                throw new BusinessException(
                        GoalErrorCode.GOAL_ASSET_ALREADY_CONNECTED
                );
            }
        }
    }



    private void validateAssets(
            Long userId,
            Goal goal,
            List<GoalAssetRequest> assets) {


        for (GoalAssetRequest asset : assets) {

            //대출 목표가 아니면 LOAN 자산 연결 불가
            if(asset.getAssetType() == AssetType.LOAN && goal.getGoalType() != GoalType.LOAN){
                throw new BusinessException(GoalErrorCode.INVALID_GOAL_ASSET);
            }

            boolean exists = switch (asset.getAssetType()) {
                case ACCOUNT -> isConnectableAccount(asset.getAssetId(), userId);
                case MONEY_BOX -> moneyBoxMapper.existsByIdAndUserId(asset.getAssetId(), userId);
                case LOAN -> true;//loanMapper.existsById(asset.getAssetId());
            };

            if (!exists) {
                throw new BusinessException(GoalErrorCode.INVALID_GOAL_ASSET);
            }
        }
    }

    @Override
    @Transactional
    public void applyStartAmount(Long goalId, Long startAmount) {
        if (startAmount == null || startAmount <= 0) {
            return;
        }

        // 시작 금액은 저금통에 담아둔 돈이라, 예적금만 연결한 목표는 반영 대상이 없다
        goalAssetMapper.findByGoalId(goalId).stream()
                .filter(asset -> asset.getAssetType() == AssetType.MONEY_BOX)
                .findFirst()
                .ifPresent(asset ->
                        moneyBoxMapper.increaseBalance(asset.getAssetId(), startAmount));
    }

    @Override
    @Transactional
    public void releaseGoalAssets(Long goalId) {
        for (GoalAsset asset : goalAssetMapper.findByGoalId(goalId)) {
            if (asset.getAssetType() != AssetType.MONEY_BOX) {
                continue;
            }

            // 저금통을 지우면 묶여 있던 금액이 소속 계좌 잔액으로 자동 복귀한다
            autoTransferMapper.deleteByMoneyBoxId(asset.getAssetId());
            moneyBoxMapper.deleteById(asset.getAssetId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long calculateCurrentAmount(Long userId, Long goalId) {
        List<GoalAsset> assets = goalAssetMapper.findByGoalId(goalId);
        Goal goal = goalMapper.findByIdAndUserId(userId, goalId);

        if(goal == null) throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);

        long totalAmount = 0L;

        for (GoalAsset asset : assets) {

            Long amount = switch (asset.getAssetType()) {
                case ACCOUNT -> findAccountBalance(asset.getAssetId());

                case MONEY_BOX -> moneyBoxMapper.findBalanceById(asset.getAssetId());

                case LOAN -> 0L; // 대출 제외
            };

            totalAmount += (amount == null ? 0L : amount);
        }
        return totalAmount;
    }

    // 목표 자산은 예적금만 허용한다. 입출금통장은 저금통이 달리는 계좌라 목표 자산이 아니다.
    private boolean isConnectableAccount(Long accountId, Long userId) {
        AccountResponse account = accountMapper.findResponseByIdAndUserId(accountId, userId);
        return account != null && !CHECKING.equals(account.getAccountType());
    }

    // 목표 연결 이후 계좌가 삭제/연동해제됐을 수 있어 null 방어
    private Long findAccountBalance(Long accountId) {
        AccountResponse account = accountMapper.findResponseById(accountId);
        return account == null ? 0L : account.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public GoalAssetListResponse getGoalAssets(Long userId, Long goalId) {
        Goal goal = goalMapper.findById(goalId);

        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }
        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException(GoalErrorCode.GOAL_ACCESS_DENIED);
        }

        List<GoalAsset> goalAssets =
                goalAssetMapper.findByGoalId(goalId);

        //잘못된 LOAN 연결 데이터 검증
        validateGoalAssetTypes(userId,goal, goalAssets);

        List<GoalAssetResponse> assets
                = goalAssets.stream()
                .map(this::convertResponse)
                .toList();

        return GoalAssetListResponse.builder()
                .assets(assets)
                .build();
    }

    // LOAN은 자산 서비스가 없어 최소 정보만 채운다.
    private GoalAssetResponse convertResponse(
            GoalAsset goalAsset
    ){
        AssetType assetType = goalAsset.getAssetType();

        if (assetType == AssetType.ACCOUNT) {
            AccountResponse account = accountMapper.findResponseById(goalAsset.getAssetId());

            if (account == null) {
                return minimalResponse(goalAsset);
            }

            return GoalAssetResponse.builder()
                    .assetType(assetType)
                    .assetId(account.getAccountId())
                    .assetName(account.getAccountName())
                    .bankName(account.getInstitutionName())
                    .accountNumberMasked(account.getMaskedAccountNumber())
                    .balance(account.getBalance())
                    .assetDetail(AssetDetailResponse.builder()
                            .interestRate(account.getInterestRate())
                            .maturityDate(account.getMaturityAt())
                            .build())
                    .autoTransfer(resolveAutoTransfer(assetType, goalAsset.getAssetId()))
                    .build();
        }

        if (assetType == AssetType.MONEY_BOX) {
            MoneyBox moneyBox = moneyBoxMapper.findById(goalAsset.getAssetId());

            if (moneyBox == null) {
                return minimalResponse(goalAsset);
            }

            // 저금통은 자체 계좌번호가 없어 소속 통장의 은행명·마스킹 번호를 쓴다
            AccountResponse ownerAccount =
                    accountMapper.findResponseById(moneyBox.getAccountId());

            return GoalAssetResponse.builder()
                    .assetType(assetType)
                    .assetId(moneyBox.getMoneyBoxId())
                    .bankName(ownerAccount == null ? null : ownerAccount.getInstitutionName())
                    .accountNumberMasked(
                            ownerAccount == null ? null : ownerAccount.getMaskedAccountNumber())
                    .balance(moneyBox.getBalance())
                    .autoTransfer(resolveAutoTransfer(assetType, goalAsset.getAssetId()))
                    .build();
        }

        return minimalResponse(goalAsset);
    }

    private GoalAssetResponse minimalResponse(GoalAsset goalAsset) {
        return GoalAssetResponse.builder()
                .assetType(goalAsset.getAssetType())
                .assetId(goalAsset.getAssetId())
                .build();
    }

    // 자동이체가 설정 안 된 자산도 있을 수 있어 null 허용
    private AutoTransferResponse resolveAutoTransfer(AssetType assetType, Long assetId) {
        AutoTransfer autoTransfer = autoTransferMapper.findByAsset(assetType, assetId);
        if (autoTransfer == null) {
            return null;
        }

        AccountResponse withdrawalAccount =
                accountMapper.findResponseById(autoTransfer.getWithdrawalAccountId());

        return AutoTransferResponse.from(autoTransfer, toWithdrawalAccountResponse(withdrawalAccount));
    }

    private WithdrawalAccountResponse toWithdrawalAccountResponse(AccountResponse account) {
        if (account == null) {
            return null;
        }
        return WithdrawalAccountResponse.builder()
                .accountId(account.getAccountId())
                .bankName(account.getInstitutionName())
                .accountNumberMasked(account.getMaskedAccountNumber())
                .build();
    }

    private void validateGoalAssetTypes(
            Long userId,
            Goal goal,
            List<GoalAsset> goalAssets
    ) {
        if (goal == null) {
            throw new BusinessException(
                    GoalErrorCode.GOAL_NOT_FOUND
            );
        }

        if (goalAssets == null || goalAssets.isEmpty()) {
            return;
        }

        for (GoalAsset asset : goalAssets) {

            if (asset == null || asset.getAssetType() == null) {
                throw new BusinessException(
                        GoalErrorCode.INVALID_GOAL_ASSET
                );
            }

            switch (asset.getAssetType()) {

                case ACCOUNT -> {
                    AccountResponse account =
                            accountMapper.findResponseByIdAndUserId(
                                    asset.getAssetId(),
                                    userId
                            );

                    if (account == null) {
                        throw new BusinessException(
                                GoalErrorCode.INVALID_GOAL_ASSET
                        );
                    }
                }

                case MONEY_BOX -> {
                    boolean exists =
                            moneyBoxMapper.existsByIdAndUserId(
                                    asset.getAssetId(),
                                    userId
                            );

                    if (!exists) {
                        throw new BusinessException(
                                GoalErrorCode.INVALID_GOAL_ASSET
                        );
                    }
                }

                case LOAN -> {
                    if (goal.getGoalType() != GoalType.LOAN) {
                        throw new BusinessException(
                                GoalErrorCode.INVALID_GOAL_ASSET
                        );
                    }

                    // loanMapper.existsByIdAndUserId()가 생기면 여기서 검증
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteGoalAsset(Long userId, Long goalId, AssetType assetType, Long assetId) {

        //목표 소유권 검증
        Goal goal = goalMapper.findByIdAndUserId(
                userId,goalId
        );

        if(goal == null){
            throw  new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        //연결된 자산인지 검증
        boolean exists = goalAssetMapper.existsByGoalIdAndAsset(goalId,assetType,assetId);

        if(!exists){
            throw new BusinessException(GoalErrorCode.GOAL_ASSET_NOT_FOUND);
        }

        goalAssetMapper.delete(
                goalId,
                assetType,
                assetId
        );


    }
}
