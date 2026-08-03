package org.jejuro.miraero.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.dto.response.SavingProductDetailResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductListResponse;
import org.jejuro.miraero.domain.product.service.SavingProductService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/savings")
public class SavingProductController {

    private final SavingProductService savingProductService;

    @GetMapping
    public ResponseEntity<ApiResponse<SavingProductListResponse>> getSavingProducts() {
        return ResponseEntity.ok(ApiResponse.success(savingProductService.getSavingProducts()));
    }

    @GetMapping("/{savingProductId}")
    public ResponseEntity<ApiResponse<SavingProductDetailResponse>> getSavingProductDetail(
            @PathVariable Long savingProductId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                savingProductService.getSavingProductDetail(savingProductId)
        ));
    }
}
