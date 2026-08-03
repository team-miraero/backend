package org.jejuro.miraero.domain.product.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SavingProductListResponse {

    private List<SavingProductResponse> products;
}
