package br.com.yago.emerald.model;

/**
 * Uma classe simples para guardar os dados de um possível encontro selvagem,
 * lidos de um arquivo JSON.
 */
public class EncounterData {
    public String speciesName;
    public int minLevel;
    public int maxLevel;
    public int chance;

    // Construtor vazio é necessário para a biblioteca LibGDX Json fazer a leitura
    public EncounterData() {}

    public EncounterData(String speciesName, int minLevel, int maxLevel, int chance) {
        this.speciesName = speciesName;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.chance = chance;
    }
}