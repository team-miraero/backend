package org.jejuro.miraero.domain.product.mapper;

import java.util.List;
import org.jejuro.miraero.domain.product.domain.DepositProduct;
import org.jejuro.miraero.domain.product.domain.DepositProductListQueryResult;

public interface DepositProductMapper {

    int upsert(DepositProduct depositProduct);

    List<DepositProductListQueryResult> findDepositProductList();
}
