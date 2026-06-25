package pe.edu.sma.restaurante;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * AgenteCliente
 * -------------
 * Argumentos (pasar al lanzar JADE):
 *   args[0] = número de mesa       (ej. "3")
 *   args[1] = items separados por coma (ej. "Lomo Saltado,Inca Kola,Arroz con Leche")
 *
 * Flujo:
 *  1. Se registra en el DF con servicio "realizar-pedido" (para que AgenteCaja pueda localizarlo).
 *  2. Busca en el DF al AgenteCocina (servicio "preparar-pedido") mediante TickerBehaviour.
 *  3. Envía el pedido al AgenteCocina.
 *  4. Espera respuestas: tiempo de espera de Cocina y factura de Caja.
 */
public class AgenteCliente extends Agent {

    private String mesa;
    private String items;

    @Override
    protected void setup() {
        // Leer argumentos
        // JADE separa por comas, así que args[0]=mesa, args[1..n]=items individuales
        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            mesa = (String) args[0];
            // Unir todos los args restantes con coma para reconstruir la lista de ítems
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) sb.append(",");
                sb.append(((String) args[i]).trim());
            }
            items = sb.toString();
        } else {
            // Valores por defecto para prueba rápida
            mesa  = "1";
            items = "Ceviche,Inca Kola,Causa Rellena";
        }

        System.out.println("[CLIENTE] Agente " + getLocalName() + " iniciado.");
        System.out.println("[CLIENTE] Mesa: " + mesa + " | Pedido: " + items);

        // 1. Registrarse en el DF para que AgenteCaja pueda encontrarlo
        ServiceDescription sd = new ServiceDescription();
        sd.setType("realizar-pedido");
        sd.setName(getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("[CLIENTE] Registrado en Páginas Amarillas: servicio 'realizar-pedido'.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // 2. Buscar AgenteCocina en el DF cada 3 segundos hasta encontrarlo
        addBehaviour(new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sdCocina = new ServiceDescription();
                sdCocina.setType("preparar-pedido");
                template.addServices(sdCocina);

                try {
                    DFAgentDescription[] resultados = DFService.search(myAgent, template);
                    if (resultados.length > 0) {
                        jade.core.AID cocina = resultados[0].getName();
                        System.out.println("[CLIENTE] AgenteCocina encontrado en DF: " + cocina.getLocalName());

                        // 3. Enviar pedido al AgenteCocina
                        ACLMessage pedido = new ACLMessage(ACLMessage.REQUEST);
                        pedido.addReceiver(cocina);
                        pedido.setContent("MESA:" + mesa + ";ITEMS:" + items);
                        send(pedido);

                        System.out.println("[CLIENTE] Pedido enviado a Cocina → MESA:" + mesa
                                + " | ITEMS:" + items);

                        stop(); // Detener el ticker una vez enviado el pedido
                    } else {
                        System.out.println("[CLIENTE] Buscando AgenteCocina en Páginas Amarillas...");
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
        });

        // 4. Comportamiento cíclico para recibir respuestas (de Cocina y de Caja)
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String contenido = msg.getContent();

                    if (contenido.startsWith("ESPERA:")) {
                        System.out.println("[CLIENTE] ✅ Tiempo de espera estimado: "
                                + contenido.replace("ESPERA:", ""));
                    } else if (contenido.startsWith("FACTURA:")) {
                        System.out.println("[CLIENTE] 🧾 Factura recibida:\n"
                                + contenido.replace("FACTURA:", ""));
                    } else {
                        System.out.println("[CLIENTE] Mensaje recibido: " + contenido);
                    }
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("[CLIENTE] Agente " + getLocalName() + " finalizado.");
    }
}
