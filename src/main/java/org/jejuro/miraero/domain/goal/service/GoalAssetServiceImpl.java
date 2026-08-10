package org.jejuro.miraero.domain.goal.service;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
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
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalAssetServiceImpl implements GoalAssetService {

    private final GoalAssetMapper goalAssetMapper;
    private final GoalMapper goalMapper;
    private final AccountMapper accountMapper;



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
                case ACCOUNT -> accountMapper.existsByIdAndUserId(asset.getAssetId(), userId);
                case MONEY_BOX -> true;//moneyBoxMapper.existsById(asset.getAssetId());
                case LOAN -> true;//loanMapper.existsById(asset.getAssetId());
            };

            if (!exists) {
                throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
                //throw new BusinessException(AssetErrorCode.ASSET_NOT_FOUND);
            }
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

                case MONEY_BOX ->
                        0L; //moneyBoxMapper.findCurrentAmount(asset.getAssetId());

                case LOAN -> 0L; // 대출 제외
            };

            totalAmount += (amount == null ? 0L : amount);
        }
        return totalAmount;
    }

    // 목표 연결 이후 계좌가 삭제/연동해제됐을 수 있어 null 방어
    private Long findAccountBalance(Long accountId) {
        AccountResponse account = accountMapper.findResponseById(accountId);
        return account == null ? 0L : account.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public GoalAssetListResponse getGoalAssets(Long goalId) {
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

    // MONEY_BOX/LOAN은 아직 자산 서비스가 없어 assetType/assetId만 반환 (범위 밖)
    private GoalAssetResponse convertResponse(
            GoalAsset goalAsset
    ){
        if (goalAsset.getAssetType() == AssetType.ACCOUNT) {
            AccountResponse account = accountMapper.findResponseById(goalAsset.getAssetId());

            if (account == null) {
                return GoalAssetResponse.builder()
                        .assetType(goalAsset.getAssetType())
                        .assetId(goalAsset.getAssetId())
                        .build();
            }

            return GoalAssetResponse.builder()
                    .assetType(goalAsset.getAssetType())
                    .assetId(account.getAccountId())
                    .assetName(account.getAccountName())
                    .bankName(account.getInstitutionName())
                    .accountNumberMasked(account.getMaskedAccountNumber())
                    .balance(account.getBalance())
                    .assetDetail(AssetDetailResponse.builder()
                            .interestRate(account.getInterestRate())
                            .maturityDate(account.getMaturityAt())
                            .build())
                    .build();
        }

        return GoalAssetResponse.builder()
                .assetType(goalAsset.getAssetType())
                .assetId(goalAsset.getAssetId())
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
