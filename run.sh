#!/bin/bash
# =====================================================
#  RestauranteSMA - Script de lanzamiento
#  Uso: ./run.sh [mesa] [item1,item2,...]
#  Ejemplo: ./run.sh 3 "Lomo Saltado,Inca Kola,Ceviche"
# =====================================================

MESA=${1:-3}
ITEMS=${2:-"Lomo Saltado,Inca Kola,Arroz con Leche"}

JAR="target/RestauranteSMA-1.0.0.jar"

if [ ! -f "$JAR" ]; then
    echo "🔨 Compilando proyecto..."
    mvn clean package -DskipTests -q
fi

echo "🚀 Lanzando Sistema Multi-Agente - Restaurante"
echo "   Mesa: $MESA | Pedido: $ITEMS"
echo ""

java -cp "$JAR" jade.Boot \
  -gui \
  -agents "cocina:pe.edu.sma.restaurante.AgenteCocina;caja:pe.edu.sma.restaurante.AgenteCaja;cliente:pe.edu.sma.restaurante.AgenteCliente($MESA,$ITEMS)"
