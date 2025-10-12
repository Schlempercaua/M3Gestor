package com.caua.madeira.dao;

import com.caua.madeira.database.Conexao;
import com.caua.madeira.model.Quote;
import com.caua.madeira.model.QuoteItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuoteDAO {
    
    public void salvar(Quote quote) throws SQLException {
        String sql = "INSERT INTO quotes (name, client_id, client_name, date, shipping_value, total_value, discount, complemento) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id;";
            
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, quote.getName());
            stmt.setInt(2, quote.getClientId());
            stmt.setString(3, quote.getClientName());
            stmt.setDate(4, Date.valueOf(quote.getDate()));
            stmt.setDouble(5, quote.getShippingValue());
            stmt.setDouble(6, quote.getTotalValue());
            stmt.setDouble(7, quote.getDiscount());
            stmt.setString(8, quote.getComplemento());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int quoteId = rs.getInt("id");
                quote.setId(quoteId);
                
                // Salva os itens do orçamento
                salvarItens(quoteId, quote.getItems(), conn);
            }
        }
    }
    
    public void atualizar(Quote quote) throws SQLException {
        String sql = "UPDATE quotes SET name = ?, client_id = ?, client_name = ?, date = ?, " +
                    "shipping_value = ?, total_value = ?, discount = ?, complemento = ? WHERE id = ?;";
            
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, quote.getName());
            stmt.setInt(2, quote.getClientId());
            stmt.setString(3, quote.getClientName());
            stmt.setDate(4, Date.valueOf(quote.getDate()));
stmt.setDouble(5, quote.getShippingValue());
            stmt.setDouble(6, quote.getTotalValue());
            stmt.setDouble(7, quote.getDiscount());
            stmt.setString(8, quote.getComplemento());
            stmt.setInt(9, quote.getId());
            
            stmt.executeUpdate();
            
            // Remove os itens antigos e adiciona os novos
            excluirItens(quote.getId(), conn);
            salvarItens(quote.getId(), quote.getItems(), conn);
        }
    }
    
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM quotes WHERE id = ?;";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Primeiro exclui os itens do orçamento
            excluirItens(id, conn);
            
            // Depois exclui o orçamento
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    public List<Quote> buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT * FROM quotes WHERE name ILIKE ? ORDER BY id DESC";
        List<Quote> orcamentos = new ArrayList<>();
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nome + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Quote quote = new Quote();
                quote.setId(rs.getInt("id"));
                quote.setName(rs.getString("name"));
                quote.setClientId(rs.getInt("client_id"));
                quote.setClientName(rs.getString("client_name"));
                quote.setDate(rs.getDate("date").toLocalDate());
                quote.setShippingValue(rs.getDouble("shipping_value"));
                quote.setTotalValue(rs.getDouble("total_value"));
                quote.setDiscount(rs.getDouble("discount"));
                
                // Carrega os itens do orçamento
                List<QuoteItem> itens = buscarItens(quote.getId());
                quote.setItems(itens);
                
                orcamentos.add(quote);
            }
        }
        
        return orcamentos;
    }
    
    public List<Quote> listarTodos() throws SQLException {
        List<Quote> quotes = new ArrayList<>();
        String sql = "SELECT * FROM quotes ORDER BY date DESC, id DESC;";
        
        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Quote quote = criarQuoteAPartirResultSet(rs);
                quotes.add(quote);
            }
        }
        
        return quotes;
    }
    
    public Quote buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM quotes WHERE id = ?;";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Quote quote = criarQuoteAPartirResultSet(rs);
                return quote;
            }
        }
        
        return null;
    }
    
    private Quote criarQuoteAPartirResultSet(ResultSet rs) throws SQLException {
        Quote quote = new Quote();
        quote.setId(rs.getInt("id"));
        quote.setName(rs.getString("name"));
        quote.setClientId(rs.getInt("client_id"));
        quote.setClientName(rs.getString("client_name"));
        quote.setDate(rs.getDate("date").toLocalDate());
        quote.setShippingValue(rs.getDouble("shipping_value"));
        quote.setTotalValue(rs.getDouble("total_value"));
        quote.setDiscount(rs.getDouble("discount"));
        quote.setComplemento(rs.getString("complemento"));
        
        // Carrega os itens do orçamento
        List<QuoteItem> itens = buscarItens(quote.getId());
        quote.setItems(itens);
        
        return quote;
    }
    
    private List<QuoteItem> buscarItens(int quoteId) throws SQLException {
        List<QuoteItem> itens = new ArrayList<>();
        String sql = "SELECT * FROM quote_items WHERE quote_id = ? ORDER BY id;";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quoteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                QuoteItem item = new QuoteItem();
                item.setId(String.valueOf(rs.getInt("id")));
                item.setQuantity(rs.getInt("quantity"));
                item.setWidth(rs.getDouble("width"));
                item.setHeight(rs.getDouble("height"));
                item.setLength(rs.getDouble("length"));
                item.setUnitValue(rs.getDouble("unit_value"));
                item.calculateTotal();
                
                itens.add(item);
            }
        }
        
        return itens;
    }
    
    private void salvarItens(int quoteId, List<QuoteItem> itens, Connection conn) throws SQLException {
        if (itens == null || itens.isEmpty()) {
            return;
        }
        
        String sql = "INSERT INTO quote_items (quote_id, quantity, width, height, length, unit_value, total) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id;";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (QuoteItem item : itens) {
                stmt.setInt(1, quoteId);
                stmt.setInt(2, item.getQuantity());
                stmt.setDouble(3, item.getWidth());
                stmt.setDouble(4, item.getHeight());
                stmt.setDouble(5, item.getLength());
                stmt.setDouble(6, item.getUnitValue());
                stmt.setDouble(7, item.getTotal());
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    item.setId(String.valueOf(rs.getInt("id")));
                }
            }
        }
    }
    
    private void excluirItens(int quoteId, Connection conn) throws SQLException {
        String sql = "DELETE FROM quote_items WHERE quote_id = ?;";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quoteId);
            stmt.executeUpdate();
        }
    }
}
