package org.jejuro.miraero.domain.product.mapper;

import java.util.List;
import org.jejuro.miraero.domain.product.domain.SavingProduct;
import org.jejuro.miraero.domain.product.domain.SavingProductListQueryResult;

public interface SavingProductMapper {

    int upsert(SavingProduct savingProduct);

    List<SavingProductListQueryResult> findSavingProductList();
}
