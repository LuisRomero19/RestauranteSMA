@echo off
REM =====================================================
REM  RestauranteSMA - Script de lanzamiento (Windows)
REM  Uso: run.bat
REM =====================================================

SET JAR=target\RestauranteSMA-1.0.0.jar

IF NOT EXIST %JAR% (
    echo Compilando proyecto...
    mvn clean package -DskipTests -q
)

echo Lanzando Sistema Multi-Agente - Restaurante
echo Mesa: 3 - Pedido: Lomo Saltado, Inca Kola, Arroz con Leche

java -cp %JAR% jade.Boot ^
  -gui ^
  -agents "cocina:pe.edu.sma.restaurante.AgenteCocina;caja:pe.edu.sma.restaurante.AgenteCaja;cliente:pe.edu.sma.restaurante.AgenteCliente(3,Lomo Saltado,Inca Kola,Arroz con Leche)"
