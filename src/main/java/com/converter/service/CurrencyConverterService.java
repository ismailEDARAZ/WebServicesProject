package com.converter.service;

import com.converter.dao.CurrencyDAO;
import com.converter.model.Currency;
import java.util.List;

public class CurrencyConverterService {
    private CurrencyDAO currencyDAO;
    
    public CurrencyConverterService() {
        this.currencyDAO = new CurrencyDAO();
    }
    
    public double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        try {
            Currency from = currencyDAO.getCurrencyByCode(fromCurrency);
            Currency to = currencyDAO.getCurrencyByCode(toCurrency);
            
            if (from == null || to == null) {
                throw new IllegalArgumentException("Devise non trouvée");
            }
            
            // Conversion via EUR (devise de base)
            double amountInEur = amount / from.getExchangeRate();
            double convertedAmount = amountInEur * to.getExchangeRate();
            
            return Math.round(convertedAmount * 100.0) / 100.0; // Arrondir à 2 décimales
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Erreur
        }
    }
    

    public String[] getAvailableCurrencies() {
        List<Currency> currencies = currencyDAO.getAllCurrencies();
        return currencies.stream()
                .map(Currency::getCode)
                .toArray(String[]::new);
    }
    

    public boolean updateExchangeRate(String currencyCode, double newRate) {
        return currencyDAO.updateCurrencyRate(currencyCode, newRate);
    }
    

    public double getExchangeRate(String currencyCode) {
        Currency currency = currencyDAO.getCurrencyByCode(currencyCode);
        return currency != null ? currency.getExchangeRate() : -1;
    }
}