# Modelo A — Monedero pospago de comisiones

> **Cobro de la comisión de la app con pago 100 % en efectivo del cliente, sin custodiar dinero de terceros.**

> ⚠️ **Aviso:** Este documento describe la arquitectura técnica y de negocio de la opción. **No constituye asesoría legal.** El encuadre regulatorio (especialmente la distinción "comisión devengada" vs. "dinero almacenado") debe ser validado por escrito con un abogado experto en regulación financiera/fintech en Colombia antes de lanzar.

---

## 1. Idea central

El cliente paga **todo en efectivo** al conductor (como ya ocurre hoy). El conductor se queda con la tarifa completa y **queda debiendo** la comisión de la plataforma (hoy 15 %). Esa comisión se acumula como una **cuenta por cobrar** (deuda del conductor con la plataforma) y se **liquida periódicamente** a través de una pasarela de pagos licenciada. Si el conductor no liquida, el **despacho deja de enviarle ofertas**.

La pregunta que todo el modelo resuelve es una sola:

> El dinero fluye cliente → conductor en efectivo, así que **el conductor termina teniendo la comisión de la plataforma**. ¿Cómo la cobramos sin almacenar dinero de terceros?

**Respuesta:** no almacenamos un saldo prepago; **cobramos un dinero que ya devengamos** (un ingreso propio).

---

## 2. Principio legal que sostiene el modelo

En Colombia el peso regulatorio está en la **custodia de dinero ajeno**, no en cobrar una tarifa:

| Actividad | Naturaleza | Carga regulatoria |
|-----------|-----------|-------------------|
| Retener un **saldo prepago** del conductor (que recarga y podría retirar) | Valor almacenado / depósito electrónico (Ley 1735/2014, SEDPE, vigilancia de la Superintendencia Financiera; roza *captación masiva*) | **Alta — es lo que queremos evitar** |
| Cobrar **nuestra propia comisión devengada** | Ingreso ordinario por servicio de intermediación tecnológica y logística | **Baja — es facturar, como cualquier empresa** |

**Conclusión:** una **deuda pospago es más liviana legalmente que un monedero prepago**, porque cobramos dinero que ya ganamos en lugar de salvaguardar el depósito de alguien. La plataforma es un **intermediario tecnológico y logístico que cobra una comisión**, no una entidad financiera.

El tramo en efectivo (cliente → conductor) **no pasa por la plataforma en ningún momento**, lo que refuerza esta postura: la plataforma nunca toca ese dinero.

---

## 3. Flujo de extremo a extremo

```
1. El cliente solicita el viaje y paga EN EFECTIVO al conductor (sin cambios).
2. Al COMPLETAR el viaje:
     - El conductor se queda con la tarifa total.
     - Se DEVENGA la comisión (15 %) como deuda del conductor.
     - Se registra un asiento en el libro de comisiones y se incrementa el saldo adeudado.
3. La deuda se ACUMULA viaje a viaje.
4. El conductor LIQUIDA su deuda (por umbral o por calendario, p. ej. semanal)
   a través de una pasarela licenciada (PSE, Nequi, Daviplata, Movii, Efecty, Baloto…).
5. Si la deuda supera el TECHO permitido, el DESPACHO deja de enviarle ofertas
   hasta que liquide. (Mismo mecanismo que el "gate" de saldo que ya existe.)
```

---

## 4. Qué reutiliza del código que ya existe

Una ventaja clave de este modelo es que **gran parte de la tubería ya está montada** (aunque hoy sea un *stub*):

- **El cliente ya paga 100 % en efectivo.** `requestTrip.ts:93` fija `payment_method: "CASH"`. No hay que cambiar el flujo del cliente.
- **La comisión ya se calcula y se muestra.** `computePrice()` en `pricing.ts` produce `commission_fee` (15 %), que se guarda en la cotización, el viaje y la oferta, y se le muestra al conductor en `OfferScreen.kt`. Hoy **nunca se cobra** — ahí es donde entra este modelo.
- **El "gate" de despacho ya existe.** `acceptOffer.ts:65` y `ToggleAvailabilityUseCase.kt:27` bloquean al conductor cuando `wallet_balance < min_wallet_balance` (5000 COP). Es exactamente el punto donde se enchufa el control de deuda (invertido: bloquear cuando la deuda supera un techo).
- **El patrón de "deuda" ya está esbozado.** `requestTrip.ts:37` bloquea a un cliente con una deuda `PENDING` en la colección `client_debts` (que hoy nadie crea). Es el mismo patrón que aplicaremos a los conductores.
- **El campo de saldo ya está protegido.** `wallet_balance` se crea en `0` (`firestore.rules:37`) y el cliente no puede escribirlo (la regla de `update` de `users` solo permite una lista blanca de campos). Un campo nuevo de comisión adeudada hereda esta protección automáticamente.

---

## 5. Modelo de datos propuesto

> Diseño conceptual, **sin implementar todavía**. Todos los montos en **pesos colombianos enteros (COP)**, siguiendo la convención del proyecto.

### 5.1. Saldo adeudado (denormalizado en `users`)

Un campo nuevo en el documento del conductor, p. ej. `commission_owed` (entero COP, solo crece con devengo y baja con liquidación). Solo escribible por Cloud Functions / Admin SDK.

> *Alternativa:* reinterpretar `wallet_balance` como un **saldo con signo** (crédito prepago positivo, deuda negativa) y bloquear cuando `wallet_balance < -max_commission_debt`. Es más compacto pero menos explícito; se recomienda el campo dedicado `commission_owed`.

### 5.2. Libro de comisiones (`driver_commission_ledger`)

Colección de asientos **inmutables**, para auditoría y conciliación:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | string | ID del asiento |
| `driver_id` | string | Conductor |
| `trip_id` | string \| null | Viaje que originó el devengo (null en liquidaciones) |
| `type` | enum | `ACCRUAL` (devengo) · `SETTLEMENT` (liquidación) · `ADJUSTMENT` (ajuste manual) |
| `amount` | int (COP) | Monto del asiento (positivo suma deuda, negativo la reduce) |
| `balance_after` | int (COP) | Saldo adeudado resultante |
| `payment_reference` | string \| null | Referencia de la pasarela (en liquidaciones) |
| `status` | enum | `PENDING` · `CONFIRMED` · `FAILED` |
| `created_at` | timestamp | Servidor |

### 5.3. Configuración (`config/system`)

Agregar el techo de deuda, junto a los parámetros que ya viven ahí (`commission_percentage`, `min_wallet_balance`):

| Campo | Descripción |
|-------|-------------|
| `max_commission_debt` | COP de deuda acumulada que dispara el bloqueo del despacho |
| `commission_settlement_period` | (opcional) cadencia de liquidación: por umbral, semanal, etc. |

---

## 6. Componentes a construir (alto nivel)

> Solo el **mapa** de lo que habría que tocar. La implementación se detalla en un plan aparte cuando se apruebe el modelo.

1. **Devengo en `completeTrip`** — dentro de la misma transacción que marca el viaje como `COMPLETED`, escribir un asiento `ACCRUAL` por `commission_fee` e incrementar `commission_owed` del conductor. Atómico con la finalización.
2. **Gate de despacho invertido** — cambiar el chequeo de `acceptOffer.ts` y `ToggleAvailabilityUseCase.kt` de `wallet_balance < min_wallet_balance` a `commission_owed >= max_commission_debt`, devolviendo un error tipo `COMMISSION_DEBT_EXCEEDED`. Conviene también que `dispatchTrip` omita a conductores por encima del techo al elegir el más cercano.
3. **Liquidación (pasarela + webhook)** — el conductor inicia el pago en la app → se crea una intención de pago en la pasarela → el conductor paga (PSE / billetera / punto de efectivo) → la pasarela llama a un Cloud Function webhook (`onCommissionPayment`, `onRequest`) → se verifica la firma → se escribe un asiento `SETTLEMENT` y se decrementa `commission_owed`. **Idempotente** por `payment_reference`.
4. **Reglas de seguridad** — `driver_commission_ledger`: lectura solo del propio conductor, escritura **solo Cloud Functions** (`allow write: if false`, igual que `quotes` / `audit_log`). `commission_owed` queda fuera de la lista blanca de `users.update`, así que ya nace protegido.
5. **UI del conductor** — pantalla de "Mi comisión / deuda": saldo adeudado, historial del libro, botón de pago y estado de bloqueo cuando supera el techo.

---

## 7. Liquidación: medios de pago (el recaudo)

La plataforma **nunca retiene el float**: el dinero de la liquidación entra directamente a la cuenta corporativa como **ingreso propio**, a través de un agregador/pasarela licenciada. Opciones en Colombia:

- **PSE / ACH** — transferencia/débito bancario.
- **Billeteras móviles** — **Nequi, Daviplata, Movii** (enormes en Colombia, con recarga en efectivo en miles de puntos).
- **Tarjetas** — Wompi, Bold, ePayco, PayU, Mercado Pago.
- **Redes de efectivo** — **Efecty, Baloto, SuRed, corresponsales bancarios.** Un conductor sin cuenta bancaria puede **pagar su comisión en efectivo en una tienda de barrio**, y el dinero llega a tu cuenta vía el agregador licenciado.

> 💡 Esta última opción es la **versión jurídicamente correcta** de la idea original de "recargar en efectivo en puntos autorizados": el efectivo recauda una **comisión adeudada** a través de una red licenciada, en lugar de **recargar un saldo prepago** que tú custodias.

Para arrancar conviene una pasarela con **buena red de recaudo en efectivo** (Efecty/Baloto/corresponsales) **+ una billetera** (Nequi/Daviplata).

---

## 8. Gestión del riesgo de crédito

El único costo real del modelo: entre que se devenga la comisión y que el conductor liquida, **la plataforma financia esa comisión**. Mitigaciones:

- **Techo de deuda bajo** (`max_commission_debt`, p. ej. 20 000–30 000 COP) que dispara el bloqueo del despacho — es el límite de crédito.
- **Cadencia de liquidación corta** (diaria o semanal) para que la exposición no se acumule.
- **Depósito de garantía opcional** (con cuidado: un depósito reembolsable que tú retienes vuelve a ser custodia → mejor que lo tenga el socio licenciado o evitarlo).
- **Suspensión y *scoring*** de conductores morosos reincidentes.

---

## 9. Requisitos transversales (obligatorios en este modelo)

- **Facturación electrónica DIAN + IVA.** La comisión es ingreso gravado: hay que emitir **facturas electrónicas** al conductor y manejar **IVA (19 %)** sobre la tarifa de servicio, además de posible **ReteFuente/ReteICA**. Decidir: factura **por viaje** vs. **consolidada periódica**. Requiere un proveedor de facturación electrónica (Siigo, Alegra, Factus, etc.). Es la pieza más olvidada y es **obligatoria**.
- **Habeas Data (Ley 1581/2012)** para el tratamiento de datos de pago e identidad.
- **Conciliación y auditoría.** Cada devengo y cada liquidación necesitan registro inmutable (el patrón de `audit_log` ya existe).
- **Validación legal** del encuadre "comisión devengada / cuenta por cobrar, no custodia" para tu estructura concreta, **antes** de lanzar.

---

## 10. Ventajas y desventajas

**A favor**

- ✅ El cliente sigue pagando **100 % en efectivo**.
- ✅ **No custodias dinero de terceros** → huella legal liviana.
- ✅ **Reutiliza** el gate de saldo y el patrón de deuda ya esbozados → lanzamiento rápido.
- ✅ Permite el **recaudo en efectivo en puntos autorizados** de forma compatible (idea original, ahora bien encuadrada).
- ✅ Escala con el negocio.

**En contra**

- ⚠️ **Riesgo de crédito**: financias la comisión hasta que el conductor liquida.
- ⚠️ Requiere **operación de cobranza y conciliación**.
- ⚠️ Depende de una **pasarela con red de recaudo**.
- ⚠️ Necesita **facturación electrónica desde el día 1**.

---

## 11. Decisiones abiertas

1. **Modelo de saldo:** campo explícito `commission_owed` (recomendado) vs. `wallet_balance` con signo.
2. **Cadencia de liquidación:** por umbral, semanal, o ambas.
3. **Pasarela(s):** ¿cuál? (priorizar red de recaudo en efectivo + billetera).
4. **Techo de deuda inicial** (`max_commission_debt`).
5. **Depósito de garantía:** ¿sí o no? ¿custodiado por el socio?
6. **Facturación:** por viaje vs. consolidada periódica.

---

## 12. Próximos pasos (sin implementar todavía)

1. Validar el marco legal con un abogado fintech en Colombia (encuadre comisión/cuenta por cobrar + facturación + IVA).
2. Elegir la pasarela/agregador de recaudo.
3. Cerrar el modelo de datos final y las decisiones abiertas de la sección 11.
4. Recién entonces, redactar el **plan de implementación** detallado (devengo en `completeTrip`, gate invertido, webhook de liquidación, reglas de seguridad, UI del conductor).
