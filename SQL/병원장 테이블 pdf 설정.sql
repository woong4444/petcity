UPDATE HOSPITAL_OWNER_REQUEST
SET DOCUMENT_URL = '/pdf/hospital_owner_request_sample.pdf'
WHERE REQUEST_ID = 8; -- << 리퀘스트 ID는 알아서 맞춰서 사용하세요

COMMIT;