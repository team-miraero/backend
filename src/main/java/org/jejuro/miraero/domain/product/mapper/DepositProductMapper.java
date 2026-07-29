package org.jejuro.miraero.domain.product.mapper;

import org.jejuro.miraero.domain.product.domain.DepositProduct;

public interface DepositProductMapper {

    int upsert(DepositProduct depositProduct);
}
