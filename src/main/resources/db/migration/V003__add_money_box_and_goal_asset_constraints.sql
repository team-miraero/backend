-- 기존 UNIQUE 제약조건 삭제
ALTER TABLE goal_asset
    DROP INDEX uk_goal_asset;

-- 새로운 UNIQUE 제약조건 추가
ALTER TABLE goal_asset
    ADD CONSTRAINT uk_goal_asset
        UNIQUE (asset_type, asset_id);


-- money_box의 balance DEFAULT 0 추가
ALTER TABLE money_box
    MODIFY COLUMN balance BIGINT NOT NULL DEFAULT 0;

-- money_box의 계좌번호해시값에 UNIQUE 제약조건 추가
ALTER TABLE money_box
    ADD CONSTRAINT uk_money_box_account_number_hash
        UNIQUE (account_number_hash);
