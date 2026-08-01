package org.jejuro.miraero.domain.goal.service;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.ErrorCode;
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
                throw new IllegalArgumentException("자산이 존재하지 않습니다. assetType: " + asset.getAssetType() + ", assetId: " + asset.getAssetId());
                //BusinessException(ErrorCode.ASSET_NOT_FOUND);
            }
        }
    }

    @Override
    public Long calculateCurrentAmount(Long goalId) {
        List<GoalAsset> assets = goalAssetMapper.findByGoalId(goalId);
        long totalAmount = 0L;
        for (GoalAsset asset : assets) {
            switch (asset.getAssetType()) {
                case ACCOUNT -> {} // totalAmount += accountMapper.findCurrentAmount(asset.getAssetId());
                case MONEY_BOX -> {} //MONEY_BOX ->totalAmount += moneyBoxMapper.findCurrentAmount(asset.getAssetId());
                case LOAN -> {
                    // 대출은 진행률 계산에서 제외
                }
                default -> throw new IllegalArgumentException("지원하지 않는 자산 타입입니다." + asset.getAssetType());
                    // throw new BusinessException(ErrorCode.INVALID_ASSET_TYPE);
            }
        }
        return totalAmount;
    }
}
