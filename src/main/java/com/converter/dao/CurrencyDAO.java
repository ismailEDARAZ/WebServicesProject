// src/main/java/com/converter/dao/CurrencyDAO.java
package com.converter.dao;

import com.converter.model.Currency;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDAO {
    private static final String XML_FILE = "currencies.xml";
    private String xmlFilePath;
    
    public CurrencyDAO() {
        initializeXmlPath();
    }
    
    private void initializeXmlPath() {
        try {
            // Méthode 1: Chercher dans le classpath (dossier resources)
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(XML_FILE);
            
            if (resourceStream != null) {
                // Copier le fichier du classpath vers un dossier accessible en écriture
                copyResourceToWorkingDirectory(resourceStream);
                resourceStream.close();
                return;
            }
            
            // Méthode 2: Chercher dans le dossier de travail de l'application
            String workingDir = System.getProperty("user.dir");
            File workingFile = new File(workingDir, XML_FILE);
            
            if (workingFile.exists()) {
                this.xmlFilePath = workingFile.getAbsolutePath();
                System.out.println("Fichier XML trouvé dans le dossier de travail: " + xmlFilePath);
                return;
            }
            
            // Méthode 3: Créer le fichier avec des données par défaut
            this.xmlFilePath = workingFile.getAbsolutePath();
            createDefaultXmlFile();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du chemin XML: " + e.getMessage());
            e.printStackTrace();
            // Créer un fichier par défaut en cas d'erreur
            createDefaultXmlFile();
        }
    }
    
    private void copyResourceToWorkingDirectory(InputStream resourceStream) {
        try {
            // Créer le fichier dans le dossier de travail
            String workingDir = System.getProperty("user.dir");
            File targetFile = new File(workingDir, XML_FILE);
            
            // Lire le contenu du fichier resource
            byte[] buffer = new byte[1024];
            int bytesRead;
            java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile);
            
            while ((bytesRead = resourceStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            
            fos.close();
            
            this.xmlFilePath = targetFile.getAbsolutePath();
            System.out.println("Fichier XML copié depuis resources vers: " + xmlFilePath);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la copie du fichier resource: " + e.getMessage());
            e.printStackTrace();
            createDefaultXmlFile();
        }
    }
    
    private void createDefaultXmlFile() {
        try {
            // Créer le fichier dans le dossier de travail de l'application
            String workingDir = System.getProperty("user.dir");
            File xmlFile = new File(workingDir, XML_FILE);
            
            // Créer le document XML par défaut
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            
            // Créer la structure XML
            Element root = document.createElement("currencies");
            document.appendChild(root);
            
            // Ajouter les devises par défaut
            addDefaultCurrency(document, root, "EUR", "Euro", "1.0");
            addDefaultCurrency(document, root, "USD", "US Dollar", "1.18");
            addDefaultCurrency(document, root, "GBP", "British Pound", "0.85");
            addDefaultCurrency(document, root, "JPY", "Japanese Yen", "130.0");
            addDefaultCurrency(document, root, "MAD", "Moroccan Dirham", "10.5");
            addDefaultCurrency(document, root, "CAD", "Canadian Dollar", "1.45");
            
            // Sauvegarder le fichier
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
            
            this.xmlFilePath = xmlFile.getAbsolutePath();
            System.out.println("Fichier XML créé avec succès: " + xmlFilePath);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du fichier XML par défaut: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addDefaultCurrency(Document document, Element root, String code, String name, String rate) {
        Element currency = document.createElement("currency");
        
        Element codeElement = document.createElement("code");
        codeElement.setTextContent(code);
        currency.appendChild(codeElement);
        
        Element nameElement = document.createElement("name");
        nameElement.setTextContent(name);
        currency.appendChild(nameElement);
        
        Element rateElement = document.createElement("rate");
        rateElement.setTextContent(rate);
        currency.appendChild(rateElement);
        
        root.appendChild(currency);
    }
    
    public List<Currency> getAllCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        
        if (xmlFilePath == null) {
            System.err.println("Chemin du fichier XML non défini");
            return getDefaultCurrencies();
        }
        
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists()) {
            System.err.println("Fichier XML n'existe pas: " + xmlFilePath);
            createDefaultXmlFile();
            xmlFile = new File(xmlFilePath);
        }
        
        // Vérifier si le fichier est vide
        if (xmlFile.length() == 0) {
            System.err.println("Fichier XML vide, recréation...");
            createDefaultXmlFile();
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            NodeList currencyNodes = document.getElementsByTagName("currency");
            
            for (int i = 0; i < currencyNodes.getLength(); i++) {
                Element currencyElement = (Element) currencyNodes.item(i);
                
                String code = getElementTextContent(currencyElement, "code");
                String name = getElementTextContent(currencyElement, "name");
                String rateStr = getElementTextContent(currencyElement, "rate");
                
                if (code != null && name != null && rateStr != null) {
                    try {
                        double rate = Double.parseDouble(rateStr);
                        currencies.add(new Currency(code, name, rate));
                    } catch (NumberFormatException e) {
                        System.err.println("Erreur de format pour le taux de " + code + ": " + rateStr);
                    }
                }
            }
            
            // Si aucune devise n'a été trouvée, retourner des devises par défaut
            if (currencies.isEmpty()) {
                System.err.println("Aucune devise trouvée dans le fichier XML, utilisation des valeurs par défaut");
                return getDefaultCurrencies();
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du fichier XML: " + e.getMessage());
            e.printStackTrace();
            return getDefaultCurrencies();
        }
        
        return currencies;
    }
    
    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
    
    private List<Currency> getDefaultCurrencies() {
        List<Currency> defaultCurrencies = new ArrayList<>();
        defaultCurrencies.add(new Currency("EUR", "Euro", 1.0));
        defaultCurrencies.add(new Currency("USD", "US Dollar", 1.18));
        defaultCurrencies.add(new Currency("GBP", "British Pound", 0.85));
        defaultCurrencies.add(new Currency("JPY", "Japanese Yen", 130.0));
        defaultCurrencies.add(new Currency("MAD", "Moroccan Dirham", 10.5));
        defaultCurrencies.add(new Currency("CAD", "Canadian Dollar", 1.45));
        return defaultCurrencies;
    }
    
    public Currency getCurrencyByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        List<Currency> currencies = getAllCurrencies();
        return currencies.stream()
                .filter(c -> c.getCode().equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }
    
    public boolean updateCurrencyRate(String code, double newRate) {
        if (xmlFilePath == null) {
            System.err.println("Impossible de mettre à jour: chemin du fichier XML non défini");
            return false;
        }
        
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists()) {
            System.err.println("Fichier XML n'existe pas pour la mise à jour: " + xmlFilePath);
            createDefaultXmlFile();
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            NodeList currencyNodes = document.getElementsByTagName("currency");
            boolean updated = false;
            
            for (int i = 0; i < currencyNodes.getLength(); i++) {
                Element currencyElement = (Element) currencyNodes.item(i);
                String currencyCode = getElementTextContent(currencyElement, "code");
                
                if (currencyCode != null && currencyCode.equalsIgnoreCase(code)) {
                    NodeList rateNodes = currencyElement.getElementsByTagName("rate");
                    if (rateNodes.getLength() > 0) {
                        rateNodes.item(0).setTextContent(String.valueOf(newRate));
                        updated = true;
                        break;
                    }
                }
            }
            
            if (updated) {
                // Sauvegarder le document
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(xmlFile);
                transformer.transform(source, result);
                
                System.out.println("Taux mis à jour avec succès pour " + code + ": " + newRate);
                return true;
            } else {
                System.err.println("Devise non trouvée pour la mise à jour: " + code);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du taux: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Méthode pour tester si le DAO fonctionne
    public void testDAO() {
        System.out.println("=== Test du CurrencyDAO ===");
        System.out.println("Chemin XML: " + xmlFilePath);
        
        List<Currency> currencies = getAllCurrencies();
        System.out.println("Nombre de devises trouvées: " + currencies.size());
        
        for (Currency currency : currencies) {
            System.out.println("- " + currency.getCode() + ": " + currency.getName() + " (" + currency.getExchangeRate() + ")");
        }
        
        System.out.println("=== Fin du test ===");
    }
}