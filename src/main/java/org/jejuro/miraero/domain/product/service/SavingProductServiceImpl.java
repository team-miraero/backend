package org.jejuro.miraero.domain.product.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.domain.ReserveType;
import org.jejuro.miraero.domain.product.domain.SavingProductListQueryResult;
import org.jejuro.miraero.domain.product.dto.response.SavingProductListResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductResponse;
import org.jejuro.miraero.domain.product.mapper.SavingProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingProductServiceImpl implements SavingProductService {

    private final SavingProductMapper savingProductMapper;

    @Override
    @Transactional(readOnly = true)
    public SavingProductListResponse getSavingProducts() {
        Map<Long, SavingProductResponse> productsById = new LinkedHashMap<>();
        Map<Long, EnumSet<ReserveType>> reserveTypesByProductId = new LinkedHashMap<>();

        for (SavingProductListQueryResult result : savingProductMapper.findSavingProductList()) {
            SavingProductResponse product = productsById.computeIfAbsent(
                    result.getSavingProductId(),
                    ignored -> new SavingProductResponse(
                            result.getSavingProductId(),
                            result.getFinancialInstitutionName(),
                            result.getProductName(),
                            result.getHighestInterestRate(),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            result.getMaxLimit(),
                            result.getJoinMethod(),
                            result.getHasJoinRestriction(),
                            result.getHasSpecialCondition()
                    )
            );
            if (!product.getSaveTerms().contains(result.getSaveTerm())) {
                product.getSaveTerms().add(result.getSaveTerm());
            }
            reserveTypesByProductId
                    .computeIfAbsent(result.getSavingProductId(), ignored -> EnumSet.noneOf(ReserveType.class))
                    .add(ReserveType.fromCode(result.getReserveType()));
        }

        for (Map.Entry<Long, SavingProductResponse> entry : productsById.entrySet()) {
            EnumSet<ReserveType> reserveTypes = reserveTypesByProductId.get(entry.getKey());
            for (ReserveType reserveType : ReserveType.values()) {
                if (reserveTypes.contains(reserveType)) {
                    entry.getValue().getReserveTypes().add(reserveType.getDisplayName());
                }
            }
        }

        return new SavingProductListResponse(new ArrayList<>(productsById.values()));
    }
}
