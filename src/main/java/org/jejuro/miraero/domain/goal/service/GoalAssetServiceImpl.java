package org.jejuro.miraero.domain.goal.service;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.dto.response.AutoTransferResponse;
import org.jejuro.miraero.domain.autotransfer.dto.response.WithdrawalAccountResponse;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.response.asset.AssetDetailResponse;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetListResponse;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetResponse;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalAssetServiceImpl implements GoalAssetService {

    private static final String CHECKING = "CHECKING";

    private final GoalAssetMapper goalAssetMapper;
    private final GoalMapper goalMapper;
    private final AccountMapper accountMapper;
    private final AutoTransferMapper autoTransferMapper;
    private final MoneyBoxMapper moneyBoxMapper;



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

        validateAssets(userId, assets);

        validateDuplicateAssets(assets);

        goalAssetMapper.saveAll(goalId, assets);
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



    private void validateAssets(Long userId, List<GoalAssetRequest> assets) {
        for (GoalAssetRequest asset : assets) {

            boolean exists = switch (asset.getAssetType()) {
                case ACCOUNT -> isConnectableAccount(asset.getAssetId(), userId);
                case MONEY_BOX -> moneyBoxMapper.existsByIdAndUserId(asset.getAssetId(), userId);
                case LOAN -> true;//loanMapper.existsById(asset.getAssetId());
            };

            if (!exists) {
                throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
                //throw new BusinessException(AssetErrorCode.ASSET_NOT_FOUND);
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
    public Long calculateCurrentAmount(Long goalId) {
        List<GoalAsset> assets = goalAssetMapper.findByGoalId(goalId);
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
