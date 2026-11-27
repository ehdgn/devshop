

-- User 데이터 (비밀번호는 예시로 '1234' 가정)
INSERT INTO user_tb (id, username, password, nickname, email, phone_number, gender, birthday, provider, created_at, updated_at) VALUES
                                                                                                                                    (1, 'user01', '$2a$10$qJJKMCr2fX38Z8szzalJC.T6w5qoDbZbOa/WdVtaGz.8d0QouXnhS', '관리자', 'admin@example.com', '010-1111-1111', 'N', '1990-01-01', 'LOCAL', NOW(), NOW()),
                                                                                                                                    (2, 'user02', '$2a$10$qJJKMCr2fX38Z8szzalJC.T6w5qoDbZbOa/WdVtaGz.8d0QouXnhS', '대박판매자', 'seller01@example.com', '010-2222-2222', 'M', '1985-05-20', 'LOCAL', NOW(), NOW()),
                                                                                                                                    (3, 'user03', '$2a$10$qJJKMCr2fX38Z8szzalJC.T6w5qoDbZbOa/WdVtaGz.8d0QouXnhS', '쇼핑왕', 'user01@example.com', '010-3333-3333', 'F', '1995-12-25', 'LOCAL', NOW(), NOW()),
                                                                                                                                    (4, 'user04', '$2a$10$qJJKMCr2fX38Z8szzalJC.T6w5qoDbZbOa/WdVtaGz.8d0QouXnhS', '새오너', 'user04@example.com', '010-4444-4444', 'F', '1995-12-25', 'LOCAL', NOW(), NOW())

;

INSERT INTO user_role_tb (role, user_id) VALUES ('ADMIN', 1), ('OWNER', 2), ('USER', 3), ('OWNER', 4);

-- Owner (판매자 등록)
INSERT INTO user_owner_tb (id, user_id, name, status) VALUES
    (1, 2, '키보드연구소', 'APPROVED'),
    (2, 4, '데스크하우스', 'APPROVED');

INSERT INTO cart_tb (amount, user_id) VALUES
                                              (0, 1),
                                              (0, 2),
                                              (0, 3);


-- 카테고리: 개발자 책상 위 물건 4가지 (하위 카테고리 없음)
INSERT INTO categories (id, category_name, depth, display_order, image_url, parent_id, created_at, updated_at) VALUES
    (1, '키보드·마우스', 1, 1, '/img/keyboard.jpg', NULL, NOW(), NOW()),
    (2, '모니터·조명',   1, 2, NULL,                NULL, NOW(), NOW()),
    (3, '책상 정리',     1, 3, '/img/palmrest.jpg', NULL, NOW(), NOW()),
    (4, '작은 소품',     1, 4, '/img/mug.jpg',      NULL, NOW(), NOW());

-- product_tb 데이터 (허용된 Enum 값: ACTIVE, SOLD_OUT 사용)
-- 사진이 없는 상품(thumbnail_url NULL)은 /img/no-image.png 로 표시됨: 실제 사진으로 교체 예정
INSERT INTO product_tb (owner_id, category_id, product_name, product_code, price, stock_quantity, minus_quantity, status, thumbnail_url, description, created_at) VALUES
    -- 키보드·마우스 (키보드연구소)
    (1, 1, 'Hyeku 알루미늄 기계식 키보드', 'KB-001', 118000, 100, 20, 'ACTIVE', 'https://www.ipopularshop.com/cdn/shop/products/00f7438d4dadcc6dd1ddf6102d96d47a.jpg?v=1688442324', '<p>통 알루미늄 하우징에 가스켓 마운트를 더해 묵직하고 조용한 타건감을 냅니다.</p><p>배열: 텐키리스 87키 / 스위치: 리니어 / 연결: 유선 USB-C</p>', NOW()),
    (1, 1, '레트로 기계식 키보드', 'KB-002', 38000, 40, 2, 'ACTIVE', 'https://coolenjoy.net/data/editor/2310/627756db3d74209960e24fee0f1ffdf276266049.jpg', '<p>베이지 톤 키캡과 동그란 키가 특징인 입문용 기계식 키보드입니다.</p><p>배열: 84키 / 스위치: 청축 / 연결: 유선</p>', NOW()),
    (1, 1, '풀배열 저소음 키보드', 'KB-003', 69000, 60, 15, 'ACTIVE', '/img/keyboard.jpg', '<p>숫자 키패드가 있는 풀배열에 저소음 적축을 넣어 사무실에서도 쓰기 좋습니다.</p><p>배열: 104키 / 스위치: 저소음 적축 / 연결: 유선</p>', NOW()),
    (1, 1, '무선 저소음 마우스', 'MS-001', 32000, 80, 30, 'ACTIVE', NULL, '<p>클릭 소리를 줄인 버튼과 손에 맞는 둥근 몸체의 무선 마우스입니다.</p><p>연결: 2.4GHz + 블루투스 / 배터리: AA 1개</p>', NOW()),
    (1, 1, 'PBT 키캡 세트', 'KC-001', 45000, 25, 8, 'ACTIVE', NULL, '<p>오래 써도 번들거리지 않는 PBT 재질의 체리 프로파일 키캡입니다.</p><p>구성: 140키 / 호환: 대부분의 MX 스위치</p>', NOW()),
    (1, 1, '아크릴 키보드 손목받침', 'PR-001', 19000, 50, 12, 'ACTIVE', '/img/palmrest.jpg', '<p>투명 아크릴 손목받침으로 키보드 높이를 맞춰 손목 부담을 줄여 줍니다.</p><p>크기: 텐키리스용 360mm</p>', NOW()),

    -- 모니터·조명 (데스크하우스)
    (2, 2, '싱글 모니터암', 'MA-001', 59000, 30, 10, 'ACTIVE', NULL, '<p>가스 스프링으로 높이와 각도를 손쉽게 조절하는 클램프형 모니터암입니다.</p><p>지원: 17~32인치, 최대 9kg</p>', NOW()),
    (2, 2, '원목 모니터 받침대', 'MR-001', 42000, 0, 18, 'SOLD_OUT', NULL, '<p>모니터를 눈높이로 올리고 아래 공간에 키보드를 넣어 둘 수 있는 원목 받침대입니다.</p><p>크기: 600 x 220 x 100mm</p>', NOW()),
    (2, 2, '모니터 조명바', 'LB-001', 39000, 45, 22, 'ACTIVE', NULL, '<p>화면에 빛 반사 없이 책상만 밝혀 주는 모니터 걸이형 조명입니다.</p><p>색온도: 2700~6500K / 전원: USB-C</p>', NOW()),
    (2, 2, '각도 조절 데스크 스탠드', 'LP-001', 35000, 40, 6, 'ACTIVE', NULL, '<p>헤드와 관절 각도를 자유롭게 바꿀 수 있는 LED 스탠드입니다.</p><p>밝기: 5단계 / 전원: 어댑터</p>', NOW()),

    -- 책상 정리 (데스크하우스)
    (2, 3, '알루미늄 노트북 거치대', 'NS-001', 29000, 70, 25, 'ACTIVE', NULL, '<p>노트북 화면을 눈높이로 올려 목 부담을 줄이는 접이식 거치대입니다.</p><p>지원: 11~17인치 / 높이 6단 조절</p>', NOW()),
    (2, 3, '가죽 데스크매트', 'DM-001', 25000, 90, 40, 'ACTIVE', NULL, '<p>키보드와 마우스를 한 번에 올릴 수 있는 넓은 양면 가죽 매트입니다.</p><p>크기: 900 x 400mm</p>', NOW()),
    (2, 3, '케이블 정리함', 'CB-001', 18000, 60, 9, 'ACTIVE', NULL, '<p>멀티탭과 남는 전선을 한 상자에 숨겨 책상 아래를 깔끔하게 만듭니다.</p><p>크기: 400 x 150 x 130mm</p>', NOW()),
    (2, 3, '헤드폰 거치대', 'HS-001', 16000, 35, 5, 'ACTIVE', NULL, '<p>책상 위에 헤드폰을 걸어 두는 알루미늄 스탠드입니다.</p><p>높이: 260mm</p>', NOW()),

    -- 작은 소품 (두 판매자)
    (2, 4, '개발자 머그컵', 'MG-001', 12000, 120, 48, 'ACTIVE', '/img/mug.jpg', '<p>커피 한 잔과 함께 버그 하나를 잡는 개발자를 위한 머그컵입니다.</p><p>용량: 350ml / 전자레인지 사용 가능</p>', NOW()),
    (2, 4, '모눈 노트 3권 세트', 'NT-001', 9000, 150, 30, 'ACTIVE', NULL, '<p>설계 스케치와 화이트보드 대신 쓰기 좋은 5mm 모눈 노트입니다.</p><p>구성: A5 3권</p>', NOW()),
    (1, 4, '개발 스티커 팩', 'ST-001', 5000, 300, 64, 'ACTIVE', NULL, '<p>노트북에 붙이기 좋은 개발 언어·도구 스티커 모음입니다.</p><p>구성: 20장</p>', NOW()),
    (1, 4, '러버덕 디버깅 오리', 'RD-001', 7000, 80, 21, 'ACTIVE', NULL, '<p>막힌 코드를 한 줄씩 설명하다 보면 답이 보이는 그 오리입니다.</p><p>크기: 60mm</p>', NOW());
