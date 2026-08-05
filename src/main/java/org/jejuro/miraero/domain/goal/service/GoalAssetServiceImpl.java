package org.jejuro.miraero.domain.goal.service;


import lombok.RequiredArgsConstructor;
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

        validateAssets(assets);

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



    private void validateAssets(List<GoalAssetRequest> assets) {
        for (GoalAssetRequest asset : assets) {

            boolean exists = switch (asset.getAssetType()) {
                case ACCOUNT -> true;//accountMapper.existsById(asset.getAssetId());
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
                case ACCOUNT ->
                        0L;//accountMapper.findCurrentAmount(asset.getAssetId());

                case MONEY_BOX ->
                        0L; //moneyBoxMapper.findCurrentAmount(asset.getAssetId());

                case LOAN -> 0L; // 대출 제외
            };

            totalAmount += (amount == null ? 0L : amount);
        }
        return totalAmount;
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

    //TODO 자산 서비스 완성되면 연결
    private GoalAssetResponse convertResponse(
            GoalAsset goalAsset
    ){

//        GoalAssetResponse response;
//
//        switch (goalAsset.getAssetType()) {
//
//
//            case ACCOUNT -> {
//
//                AccountResponse account =
//                        accountService.findById(
//                                goalAsset.getAssetId()
//                        );
//
//
//                response =
//                        GoalAssetResponse.builder()
//                                .assetType(
//                                        goalAsset.getAssetType()
//                                )
//                                .assetId(
//                                        account.getAccountId()
//                                )
//                                .assetName(
//                                        account.getAccountName()
//                                )
//                                .bankName(
//                                        account.getBankName()
//                                )
//                                .accountNumberMasked(
//                                        account.getAccountNumberMasked()
//                                )
//                                .balance(
//                                        account.getBalance()
//                                )
//                                .assetDetail(
//                                        AssetDetailResponse.builder()
//                                                .interestRate(
//                                                        account.getInterestRate()
//                                                )
//                                                .maturityDate(
//                                                        account.getMaturityDate()
//                                                )
//                                                .build()
//                                )
//                                .build();
//            }
//
//
//            case MONEY_BOX -> {
//
//                MoneyBoxResponse moneyBox =
//                        moneyBoxService.findById(
//                                goalAsset.getAssetId()
//                        );
//
//
//                response =
//                        GoalAssetResponse.builder()
//                                .assetType(
//                                        goalAsset.getAssetType()
//                                )
//                                .assetId(
//                                        moneyBox.getMoneyBoxId()
//                                )
//                                .assetName(
//                                        moneyBox.getName()
//                                )
//                                .bankName(
//                                        moneyBox.getBankName()
//                                )
//                                .accountNumberMasked(
//                                        moneyBox.getAccountNumberMasked()
//                                )
//                                .balance(
//                                        moneyBox.getBalance()
//                                )
//                                .build();
//
//            }
//
//
//            case LOAN -> {
//
//                LoanResponse loan =
//                        loanService.findById(
//                                goalAsset.getAssetId()
//                        );
//
//
//                response =
//                        GoalAssetResponse.builder()
//                                .assetType(
//                                        goalAsset.getAssetType()
//                                )
//                                .assetId(
//                                        loan.getLoanId()
//                                )
//                                .assetName(
//                                        loan.getLoanName()
//                                )
//                                .bankName(
//                                        loan.getBankName()
//                                )
//                                .balance(
//                                        loan.getBalance()
//                                )
//                                .build();
//
//            }
//
//
//            default -> throw new IllegalArgumentException();
//        }
//
//        AutoTransferResponse autoTransfer =
//                autoTransferService.getByAsset(
//                        goalAsset.getAssetType(),
//                        goalAsset.getAssetId()
//                );
//
//        return GoalAssetResponse.builder()
//                .assetType(response.getAssetType())
//                .assetId(response.getAssetId())
//                .assetName(response.getAssetName())
//                .bankName(response.getBankName())
//                .accountNumberMasked(response.getAccountNumberMasked())
//                .balance(response.getBalance())
//                .assetDetail(response.getAssetDetail())
//                .autoTransfer(autoTransfer)
//                .build();

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
