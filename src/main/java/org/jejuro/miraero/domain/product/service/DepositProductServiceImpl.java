package org.jejuro.miraero.domain.product.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.domain.DepositProductListQueryResult;
import org.jejuro.miraero.domain.product.dto.response.DepositProductListResponse;
import org.jejuro.miraero.domain.product.dto.response.DepositProductResponse;
import org.jejuro.miraero.domain.product.mapper.DepositProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositProductServiceImpl implements DepositProductService {

    private final DepositProductMapper depositProductMapper;

    @Override
    @Transactional(readOnly = true)
    public DepositProductListResponse getDepositProducts() {
        Map<Long, DepositProductResponse> productsById = new LinkedHashMap<>();

        for (DepositProductListQueryResult result : depositProductMapper.findDepositProductList()) {
            DepositProductResponse product = productsById.computeIfAbsent(
                    result.getDepositProductId(),
                    ignored -> new DepositProductResponse(
                            result.getDepositProductId(),
                            result.getFinancialInstitutionName(),
                            result.getProductName(),
                            result.getMaxInterestRate(),
                            new ArrayList<>(),
                            result.getMaxLimit(),
                            result.getJoinMethod(),
                            result.getHasJoinRestriction(),
                            result.getHasSpecialCondition()
                    )
            );
            product.getSaveTerms().add(result.getSaveTerm());
        }

        return new DepositProductListResponse(new ArrayList<>(productsById.values()));
    }
}
