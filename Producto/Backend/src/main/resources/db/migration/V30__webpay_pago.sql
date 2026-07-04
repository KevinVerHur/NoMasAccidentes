-- RF09: pago en línea de una cuota por parte del cliente vía Webpay Plus
-- (Transbank). Se persiste el token de la transacción para correlacionar el
-- retorno del navegador con la cuota, y la orden de compra enviada a Transbank.
-- Ambas columnas son nullables: solo se llenan cuando la cuota se paga por Webpay.

ALTER TABLE pago
    ADD COLUMN webpay_token        VARCHAR(100) NULL AFTER medio_pago,
    ADD COLUMN webpay_orden_compra VARCHAR(50)  NULL AFTER webpay_token;
