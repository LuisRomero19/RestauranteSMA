package pe.edu.sma.restaurante;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;

/**
 * AgenteCocina
 * ------------
 * - Se registra en las Páginas Amarillas ofreciendo el servicio "preparar-pedido".
 * - Recibe pedidos del AgenteCliente (formato: "MESA:X;ITEMS:item1,item2,...").
 * - Calcula el tiempo estimado de preparación (5 min por ítem).
 * - Busca en el DF al AgenteCaja y le reenvía el resumen del pedido para facturación.
 * - Responde al AgenteCliente con el tiempo estimado de espera.
 */
public class AgenteCocina extends Agent {

    @Override
    protected void setup() {
        System.out.println("[COCINA] Agente " + getLocalName() + " iniciado.");

        // 1. Registrar servicio en Páginas Amarillas (DF)
        ServiceDescription sd = new ServiceDescription();
        sd.setType("preparar-pedido");
        sd.setName(getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("[COCINA] Registrado en Páginas Amarillas: servicio 'preparar-pedido'.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // 2. Comportamiento cíclico: esperar pedidos del AgenteCliente
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String contenido = msg.getContent();
                    System.out.println("[COCINA] Pedido recibido: " + contenido);

                    // Parsear pedido: "MESA:3;ITEMS:Lomo Saltado,Inca Kola,Arroz con Leche"
                    String[] partes = contenido.split(";");
                    String mesa = partes[0].replace("MESA:", "").trim();
                    String itemsStr = partes[1].replace("ITEMS:", "").trim();
                    String[] items = itemsStr.split(",");
                    int tiempoEstimado = items.length * 5; // 5 min por ítem

                    System.out.println("[COCINA] Preparando " + items.length +
                            " ítem(s) para mesa " + mesa +
                            ". Tiempo estimado: " + tiempoEstimado + " min.");

                    // 3. Responder al AgenteCliente con tiempo de espera
                    ACLMessage respuestaCliente = msg.createReply();
                    respuestaCliente.setPerformative(ACLMessage.INFORM);
                    respuestaCliente.setContent("ESPERA:" + tiempoEstimado + " minutos para mesa " + mesa);
                    send(respuestaCliente);

                    // 4. Buscar AgenteCaja en el DF y enviarle el pedido para facturar
                    notificarCaja(mesa, items, tiempoEstimado);

                } else {
                    block();
                }
            }
        });
    }

    /**
     * Busca al AgenteCaja en las Páginas Amarillas y le envía el pedido procesado.
     */
    private void notificarCaja(String mesa, String[] items, int tiempoEstimado) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sdCaja = new ServiceDescription();
        sdCaja.setType("facturar-pedido");
        template.addServices(sdCaja);

        try {
            DFAgentDescription[] resultados = DFService.search(this, template);
            if (resultados.length > 0) {
                AID caja = resultados[0].getName();
                ACLMessage msgCaja = new ACLMessage(ACLMessage.REQUEST);
                msgCaja.addReceiver(caja);

                // Armar mensaje para caja: mesa + items separados por '|'
                String payload = "MESA:" + mesa + ";ITEMS:" + String.join("|", items) +
                                 ";TIEMPO:" + tiempoEstimado;
                msgCaja.setContent(payload);
                send(msgCaja);
                System.out.println("[COCINA] Pedido enviado a AgenteCaja (" + caja.getLocalName() + ").");
            } else {
                System.out.println("[COCINA] ADVERTENCIA: No se encontró AgenteCaja en el DF.");
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("[COCINA] Agente " + getLocalName() + " finalizado.");
    }
}
