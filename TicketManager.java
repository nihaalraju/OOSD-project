package com.supportticket;

import java.util.*;
import java.util.stream.Collectors;

public class TicketManager {
    // Primary storage: HashMap for O(1) lookup by ticket ID
    private HashMap<String, Ticket> ticketMap;
    // Ordered list for display
    private ArrayList<Ticket> ticketList;
    // Priority queue for escalation
    private PriorityQueue<Ticket> priorityQueue;
    // Statistics tracking
    private HashMap<String, Integer> categoryStats;
    private HashMap<String, Integer> statusStats;

    public TicketManager() {
        ticketMap = new HashMap<>();
        ticketList = new ArrayList<>();
        priorityQueue = new PriorityQueue<>(
            Comparator.comparingInt(t -> getPriorityLevel(t.getPriority()))
        );
        categoryStats = new HashMap<>();
        statusStats = new HashMap<>();
        loadSampleData();
    }

    private int getPriorityLevel(String priority) {
        switch (priority) {
            case "Critical": return 0;
            case "High":     return 1;
            case "Medium":   return 2;
            case "Low":      return 3;
            default:         return 4;
        }
    }

    private void loadSampleData() {
        String[][] samples = {
            {"Alice Johnson", "alice@email.com", "Technical", "High", "Login page not loading on Chrome"},
            {"Bob Smith", "bob@email.com", "Billing", "Critical", "Double charged for subscription"},
            {"Carol White", "carol@email.com", "General", "Low", "Need help resetting password"},
            {"David Brown", "david@email.com", "Technical", "Medium", "App crashes on mobile"},
            {"Eva Martinez", "eva@email.com", "Billing", "High", "Refund request for cancelled order"}
        };
        for (String[] s : samples) {
            Ticket t = new Ticket(s[0], s[1], s[2], s[3], s[4]);
            addTicket(t);
        }
        ticketList.get(2).setStatus("Resolved");
        ticketList.get(3).setStatus("In Progress");
    }

    public void addTicket(Ticket ticket) {
        ticketMap.put(ticket.getTicketId(), ticket);
        ticketList.add(ticket);
        priorityQueue.offer(ticket);
        categoryStats.merge(ticket.getCategory(), 1, Integer::sum);
        statusStats.merge(ticket.getStatus(), 1, Integer::sum);
    }

    public boolean deleteTicket(String ticketId) {
        Ticket t = ticketMap.remove(ticketId);
        if (t != null) {
            ticketList.remove(t);
            categoryStats.merge(t.getCategory(), -1, Integer::sum);
            return true;
        }
        return false;
    }

    public Ticket findById(String ticketId) {
        return ticketMap.get(ticketId);
    }

    public ArrayList<Ticket> searchTickets(String keyword) {
        String kw = keyword.toLowerCase();
        return ticketList.stream()
            .filter(t -> t.getCustomerName().toLowerCase().contains(kw)
                      || t.getTicketId().toLowerCase().contains(kw)
                      || t.getCategory().toLowerCase().contains(kw)
                      || t.getDescription().toLowerCase().contains(kw))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Ticket> filterByStatus(String status) {
        if (status.equals("All")) return ticketList;
        return ticketList.stream()
            .filter(t -> t.getStatus().equals(status))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Ticket> filterByPriority(String priority) {
        if (priority.equals("All")) return ticketList;
        return ticketList.stream()
            .filter(t -> t.getPriority().equals(priority))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Ticket> getAllTickets() { return ticketList; }

    public HashMap<String, Integer> getCategoryStats() { return categoryStats; }

    public long getCountByStatus(String status) {
        return ticketList.stream().filter(t -> t.getStatus().equals(status)).count();
    }
}
