package pe.edu.sma.restaurante;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * AgenteCaja
 * ----------
 * - Se registra en las Páginas Amarillas ofreciendo el servicio "facturar-pedido".
 * - Recibe pedidos procesados de AgenteCocina.
 * - Calcula el total en soles usando un menú de precios predefinido.
 * - Busca al AgenteCliente en el DF y le envía la factura con el desglose de precios.
 */
public class AgenteCaja extends Agent {

    // Menú de precios (soles)
    private static final Map<String, Double> MENU = new HashMap<>();

    static {
        MENU.put("Lomo Saltado",       28.0);
        MENU.put("Pollo a la Brasa",   22.0);
        MENU.put("Ceviche",            32.0);
        MENU.put("Arroz con Leche",    10.0);
        MENU.put("Inca Kola",           5.0);
        MENU.put("Agua Mineral",        4.0);
        MENU.put("Causa Rellena",      18.0);
        MENU.put("Ají de Gallina",     24.0);
    }

    @Override
    protected void setup() {
        System.out.println("[CAJA] Agente " + getLocalName() + " iniciado.");

        // 1. Registrar servicio en Páginas Amarillas (DF)
        ServiceDescription sd = new ServiceDescription();
        sd.setType("facturar-pedido");
        sd.setName(getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("[CAJA] Registrado en Páginas Amarillas: servicio 'facturar-pedido'.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // 2. Comportamiento cíclico: esperar pedidos de AgenteCocina
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String contenido = msg.getContent();
                    System.out.println("[CAJA] Datos recibidos de Cocina: " + contenido);

                    // Parsear: "MESA:3;ITEMS:Lomo Saltado|Inca Kola|Arroz con Leche;TIEMPO:15"
                    String[] partes = contenido.split(";");
                    String mesa = partes[0].replace("MESA:", "").trim();
                    String[] items = partes[1].replace("ITEMS:", "").trim().split("\\|");
                    String tiempo = partes[2].replace("TIEMPO:", "").trim();

                    // Calcular subtotales y total
                    StringBuilder desglose = new StringBuilder();
                    double total = 0.0;

                    desglose.append("=== FACTURA - MESA ").append(mesa).append(" ===\n");
                    for (String item : items) {
                        item = item.trim();
                        double precio = MENU.getOrDefault(item, 15.0); // precio default si no está en menú
                        total += precio;
                        desglose.append(String.format("  %-22s S/ %.2f\n", item, precio));
                    }

                    double igv = total * 0.18;
                    double totalConIgv = total + igv;

                    desglose.append("--------------------------------\n");
                    desglose.append(String.format("  %-22s S/ %.2f\n", "Subtotal:", total));
                    desglose.append(String.format("  %-22s S/ %.2f\n", "IGV (18%):", igv));
                    desglose.append(String.format("  %-22s S/ %.2f\n", "TOTAL:", totalConIgv));
                    desglose.append("  Tiempo espera: ").append(tiempo).append(" min\n");
                    desglose.append("================================");

                    System.out.println("[CAJA] Factura generada:\n" + desglose);

                    // 3. Buscar AgenteCliente en el DF y enviarle la factura
                    enviarFacturaAlCliente(mesa, desglose.toString());

                } else {
                    block();
                }
            }
        });
    }

    /**
     * Busca al AgenteCliente en el DF por el servicio "realizar-pedido" y le envía la factura.
     */
    private void enviarFacturaAlCliente(String mesa, String factura) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sdCliente = new ServiceDescription();
        sdCliente.setType("realizar-pedido");
        template.addServices(sdCliente);

        try {
            DFAgentDescription[] resultados = DFService.search(this, template);
            if (resultados.length > 0) {
                AID cliente = resultados[0].getName();
                ACLMessage msgCliente = new ACLMessage(ACLMessage.INFORM);
                msgCliente.addReceiver(cliente);
                msgCliente.setContent("FACTURA:" + factura);
                send(msgCliente);
                System.out.println("[CAJA] Factura enviada a AgenteCliente (" + cliente.getLocalName() + ").");
            } else {
                System.out.println("[CAJA] ADVERTENCIA: No se encontró AgenteCliente en el DF.");
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
        System.out.println("[CAJA] Agente " + getLocalName() + " finalizado.");
    }
}
