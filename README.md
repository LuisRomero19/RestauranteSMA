# 🍽️ Sistema Multi-Agente — Gestión de Restaurante (JADE)

## Descripción del Problema

En un restaurante tradicional, la coordinación entre el cliente, la cocina y la caja involucra múltiples actores que deben comunicarse de forma eficiente. Los retrasos, errores de comunicación y falta de visibilidad del estado del pedido son problemas frecuentes.

## Solución Propuesta

Un **Sistema Multi-Agente (SMA)** implementado en **JADE** que simula el flujo completo de atención de un restaurante. Cada agente es autónomo, tiene su propia lógica de negocio, y colabora con los demás mediante el intercambio de mensajes ACL y el uso de **Páginas Amarillas (DF — Directory Facilitator)**.

---

## 🏗️ Arquitectura del Sistema

```
AgenteCliente
    │
    │  1. Busca "preparar-pedido" en DF (Páginas Amarillas)
    │  2. Envía pedido (MESA + ITEMS)
    ▼
AgenteCocina  ◄── Registrado en DF como "preparar-pedido"
    │
    │  3. Calcula tiempo estimado (5 min/ítem)
    │  4. Responde al Cliente con tiempo de espera
    │  5. Busca "facturar-pedido" en DF
    │  6. Envía resumen a AgenteCaja
    ▼
AgenteCaja  ◄── Registrado en DF como "facturar-pedido"
    │
    │  7. Calcula subtotales + IGV (18%)
    │  8. Busca "realizar-pedido" en DF
    │  9. Envía factura al AgenteCliente
    ▼
AgenteCliente  ◄── Registrado en DF como "realizar-pedido"
    │
    │ 10. Muestra tiempo de espera
    │ 11. Muestra factura completa
```

---

## 👾 Agentes y Lógica de Negocio

| Agente | Servicio en DF | Lógica propia |
|--------|---------------|---------------|
| `AgenteCliente` | `realizar-pedido` | Busca cocina en DF, envía pedido con mesa e ítems, recibe tiempo de espera y factura |
| `AgenteCocina` | `preparar-pedido` | Calcula tiempo estimado (5 min/ítem), notifica al cliente y busca la caja |
| `AgenteCaja` | `facturar-pedido` | Calcula precios por ítem + IGV 18%, genera factura y la envía al cliente |

---

## 💰 Menú de Precios

| Plato | Precio (S/) |
|-------|------------|
| Lomo Saltado | 28.00 |
| Pollo a la Brasa | 22.00 |
| Ceviche | 32.00 |
| Causa Rellena | 18.00 |
| Ají de Gallina | 24.00 |
| Arroz con Leche | 10.00 |
| Inca Kola | 5.00 |
| Agua Mineral | 4.00 |

---

## ⚙️ Requisitos

- Java 11+
- Maven 3.6+
- Conexión a internet (para descargar JADE desde JitPack la primera vez)

---

## 🚀 Compilar y Ejecutar

### 1. Compilar

```bash
cd RestauranteSMA
mvn clean package -DskipTests
```

### 2. Ejecutar (con GUI de JADE)

```bash
# Linux / macOS
java -cp target/RestauranteSMA-1.0.0.jar jade.Boot \
  -gui \
  -agents "cocina:pe.edu.sma.restaurante.AgenteCocina;caja:pe.edu.sma.restaurante.AgenteCaja;cliente:pe.edu.sma.restaurante.AgenteCliente(3,Lomo Saltado,Inca Kola,Arroz con Leche)"

# Windows
java -cp target\RestauranteSMA-1.0.0.jar jade.Boot ^
  -gui ^
  -agents "cocina:pe.edu.sma.restaurante.AgenteCocina;caja:pe.edu.sma.restaurante.AgenteCaja;cliente:pe.edu.sma.restaurante.AgenteCliente(3,Lomo Saltado,Inca Kola,Arroz con Leche)"
```

### 3. Ejecutar con el script (Linux/macOS)

```bash
chmod +x run.sh
./run.sh
```

---

## 📋 Formato de Argumentos del AgenteCliente

```
AgenteCliente(MESA, ITEM1, ITEM2, ...)
```

Ejemplo:
```
cliente:pe.edu.sma.restaurante.AgenteCliente(5,Ceviche,Pollo a la Brasa,Inca Kola)
```

---

## 📁 Estructura del Proyecto

```
RestauranteSMA/
├── pom.xml
├── run.sh
├── run.bat
├── README.md
└── src/
    └── main/
        └── java/
            └── pe/edu/sma/restaurante/
                ├── AgenteCliente.java
                ├── AgenteCocina.java
                └── AgenteCaja.java
```

---

## 📊 Salida Esperada en Consola

```
[COCINA] Registrado en Páginas Amarillas: servicio 'preparar-pedido'.
[CAJA]   Registrado en Páginas Amarillas: servicio 'facturar-pedido'.
[CLIENTE] Registrado en Páginas Amarillas: servicio 'realizar-pedido'.
[CLIENTE] AgenteCocina encontrado en DF: cocina
[CLIENTE] Pedido enviado a Cocina → MESA:3 | ITEMS:Lomo Saltado,Inca Kola,Arroz con Leche
[COCINA]  Pedido recibido: MESA:3;ITEMS:Lomo Saltado,Inca Kola,Arroz con Leche
[COCINA]  Preparando 3 ítem(s) para mesa 3. Tiempo estimado: 15 min.
[COCINA]  Pedido enviado a AgenteCaja (caja).
[CLIENTE] ✅ Tiempo de espera estimado: 15 minutos para mesa 3
[CAJA]    Datos recibidos de Cocina: MESA:3;ITEMS:Lomo Saltado|Inca Kola|Arroz con Leche;TIEMPO:15
[CAJA]    Factura generada:
=== FACTURA - MESA 3 ===
  Lomo Saltado           S/ 28.00
  Inca Kola              S/  5.00
  Arroz con Leche        S/ 10.00
--------------------------------
  Subtotal:              S/ 43.00
  IGV (18%):             S/  7.74
  TOTAL:                 S/ 50.74
  Tiempo espera: 15 min
================================
[CLIENTE] 🧾 Factura recibida: ...
```

---

## 👥 Equipo

- [Nombre 1]
- [Nombre 2]
- [Nombre 3 — Luis Romero]

**Repositorio:** https://github.com/[org]/RestauranteSMA  
**Video Demo:** https://youtu.be/[id]

---

## 📚 Referencias

- JADE Framework: https://jade.tilab.com  
- Tutorial JADE para principiantes: incluido en `/JADEProgramming-Tutorial-for-beginners.pdf`
