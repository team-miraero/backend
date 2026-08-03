package org.jejuro.miraero.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.dto.response.DepositProductDetailResponse;
import org.jejuro.miraero.domain.product.dto.response.DepositProductListResponse;
import org.jejuro.miraero.domain.product.service.DepositProductService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits")
public class DepositProductController {

    private final DepositProductService depositProductService;

    @GetMapping
    public ResponseEntity<ApiResponse<DepositProductListResponse>> getDepositProducts() {
        return ResponseEntity.ok(ApiResponse.success(depositProductService.getDepositProducts()));
    }

    @GetMapping("/{depositProductId}")
    public ResponseEntity<ApiResponse<DepositProductDetailResponse>> getDepositProductDetail(
            @PathVariable Long depositProductId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                depositProductService.getDepositProductDetail(depositProductId)
        ));
    }
}
