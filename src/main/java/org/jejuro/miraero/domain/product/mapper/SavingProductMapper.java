package org.jejuro.miraero.domain.product.mapper;

import org.jejuro.miraero.domain.product.domain.SavingProduct;

public interface SavingProductMapper {

    int upsert(SavingProduct savingProduct);
}
