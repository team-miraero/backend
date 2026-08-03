package org.jejuro.miraero.domain.goal.service;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalAssetServiceImpl implements GoalAssetService {

    private final GoalAssetMapper goalAssetMapper;
    // private final AccountMapper accountMapper;
    // private final MoneyBoxMapper moneyBoxMapper;
    // private final LoanMapper loanMapper;




    @Override
    @Transactional
    public void saveGoalAssets(Long goalId, List<GoalAssetRequest> assets) {

        if(assets == null || assets.isEmpty()){
            return;
        }

        validateAssets(assets);

        goalAssetMapper.saveAll(goalId, assets);
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
}
