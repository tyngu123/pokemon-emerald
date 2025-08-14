package br.com.yago.emerald.model;

import java.util.EnumMap;
import java.util.Map;

public class TypeChart {

	private static final Map<PokemonType, Map<PokemonType, Float>> chart = new EnumMap<>(PokemonType.class);

	static {
		// Inicializa todos os tipos com efetividade 1.0f (dano normal)
		for (PokemonType t1 : PokemonType.values()) {
			chart.put(t1, new EnumMap<>(PokemonType.class));
			for (PokemonType t2 : PokemonType.values()) {
				chart.get(t1).put(t2, 1.0f);
			}
		}

		// --- EFETIVIDADES GEN 3 (R/S/E/FR/LG) ---

		// NORMAL
		setEffectiveness(PokemonType.NORMAL, PokemonType.ROCK, 0.5f);
		setEffectiveness(PokemonType.NORMAL, PokemonType.STEEL, 0.5f);
		setEffectiveness(PokemonType.NORMAL, PokemonType.GHOST, 0.0f);

		// FIRE
		setEffectiveness(PokemonType.FIRE, PokemonType.FIRE, 0.5f);
		setEffectiveness(PokemonType.FIRE, PokemonType.WATER, 0.5f);
		setEffectiveness(PokemonType.FIRE, PokemonType.GRASS, 2.0f);
		setEffectiveness(PokemonType.FIRE, PokemonType.ICE, 2.0f);
		setEffectiveness(PokemonType.FIRE, PokemonType.BUG, 2.0f);
		setEffectiveness(PokemonType.FIRE, PokemonType.ROCK, 0.5f);
		setEffectiveness(PokemonType.FIRE, PokemonType.DRAGON, 0.5f);
		setEffectiveness(PokemonType.FIRE, PokemonType.STEEL, 2.0f);

		// WATER
		setEffectiveness(PokemonType.WATER, PokemonType.FIRE, 2.0f);
		setEffectiveness(PokemonType.WATER, PokemonType.WATER, 0.5f);
		setEffectiveness(PokemonType.WATER, PokemonType.GRASS, 0.5f);
		setEffectiveness(PokemonType.WATER, PokemonType.GROUND, 2.0f);
		setEffectiveness(PokemonType.WATER, PokemonType.ROCK, 2.0f);
		setEffectiveness(PokemonType.WATER, PokemonType.DRAGON, 0.5f);

		// ELECTRIC
		setEffectiveness(PokemonType.ELECTRIC, PokemonType.WATER, 2.0f);
		setEffectiveness(PokemonType.ELECTRIC, PokemonType.ELECTRIC, 0.5f);
		setEffectiveness(PokemonType.ELECTRIC, PokemonType.GRASS, 0.5f);
		setEffectiveness(PokemonType.ELECTRIC, PokemonType.GROUND, 0.0f);
		setEffectiveness(PokemonType.ELECTRIC, PokemonType.FLYING, 2.0f);
		setEffectiveness(PokemonType.ELECTRIC, PokemonType.DRAGON, 0.5f);

		// GRASS
		setEffectiveness(PokemonType.GRASS, PokemonType.FIRE, 0.5f);
		setEffectiveness(PokemonType.GRASS, PokemonType.WATER, 2.0f);
		setEffectiveness(PokemonType.GRASS, PokemonType.GRASS, 0.5f);
		setEffectiveness(PokemonType.GRASS, PokemonType.POISON, 0.5f);
		setEffectiveness(PokemonType.GRASS, PokemonType.GROUND, 2.0f);
		setEffectiveness(PokemonType.GRASS, PokemonType.FLYING, 0.5f);
		setEffectiveness(PokemonType.GRASS, PokemonType.BUG, 0.5f);
		setEffectiveness(PokemonType.GRASS, PokemonType.ROCK, 2.0f);
		setEffectiveness(PokemonType.GRASS, PokemonType.DRAGON, 0.5f);
		setEffectiveness(PokemonType.GRASS, PokemonType.STEEL, 0.5f);

		// ICE
		setEffectiveness(PokemonType.ICE, PokemonType.FIRE, 0.5f);
		setEffectiveness(PokemonType.ICE, PokemonType.WATER, 0.5f);
		setEffectiveness(PokemonType.ICE, PokemonType.GRASS, 2.0f);
		setEffectiveness(PokemonType.ICE, PokemonType.GROUND, 2.0f);
		setEffectiveness(PokemonType.ICE, PokemonType.FLYING, 2.0f);
		setEffectiveness(PokemonType.ICE, PokemonType.DRAGON, 2.0f);
		setEffectiveness(PokemonType.ICE, PokemonType.STEEL, 0.5f);
		setEffectiveness(PokemonType.ICE, PokemonType.ICE, 0.5f);

		// FIGHTING
		setEffectiveness(PokemonType.FIGHTING, PokemonType.NORMAL, 2.0f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.ICE, 2.0f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.POISON, 0.5f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.FLYING, 0.5f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.PSYCHIC, 0.5f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.BUG, 0.5f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.ROCK, 2.0f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.GHOST, 0.0f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.DARK, 2.0f);
		setEffectiveness(PokemonType.FIGHTING, PokemonType.STEEL, 2.0f);

		// POISON
		setEffectiveness(PokemonType.POISON, PokemonType.GRASS, 2.0f);
		setEffectiveness(PokemonType.POISON, PokemonType.POISON, 0.5f);
		setEffectiveness(PokemonType.POISON, PokemonType.GROUND, 0.5f);
		setEffectiveness(PokemonType.POISON, PokemonType.ROCK, 0.5f);
		setEffectiveness(PokemonType.POISON, PokemonType.GHOST, 0.5f);
		setEffectiveness(PokemonType.POISON, PokemonType.STEEL, 0.0f);

		// GROUND
		setEffectiveness(PokemonType.GROUND, PokemonType.FIRE, 2.0f);
		setEffectiveness(PokemonType.GROUND, PokemonType.ELECTRIC, 2.0f);
		setEffectiveness(PokemonType.GROUND, PokemonType.GRASS, 0.5f);
		setEffectiveness(PokemonType.GROUND, PokemonType.POISON, 2.0f);
		setEffectiveness(PokemonType.GROUND, PokemonType.FLYING, 0.0f);
		setEffectiveness(PokemonType.GROUND, PokemonType.BUG, 0.5f);
		setEffectiveness(PokemonType.GROUND, PokemonType.ROCK, 2.0f);
		setEffectiveness(PokemonType.GROUND, PokemonType.STEEL, 2.0f);

		// FLYING
		setEffectiveness(PokemonType.FLYING, PokemonType.ELECTRIC, 0.5f);
		setEffectiveness(PokemonType.FLYING, PokemonType.GRASS, 2.0f);
		setEffectiveness(PokemonType.FLYING, PokemonType.FIGHTING, 2.0f);
		setEffectiveness(PokemonType.FLYING, PokemonType.BUG, 2.0f);
		setEffectiveness(PokemonType.FLYING, PokemonType.ROCK, 0.5f);
		setEffectiveness(PokemonType.FLYING, PokemonType.STEEL, 0.5f);

		// PSYCHIC
		setEffectiveness(PokemonType.PSYCHIC, PokemonType.FIGHTING, 2.0f);
		setEffectiveness(PokemonType.PSYCHIC, PokemonType.POISON, 2.0f);
		setEffectiveness(PokemonType.PSYCHIC, PokemonType.PSYCHIC, 0.5f);
		setEffectiveness(PokemonType.PSYCHIC, PokemonType.DARK, 0.0f);
		setEffectiveness(PokemonType.PSYCHIC, PokemonType.STEEL, 0.5f);

		// BUG
		setEffectiveness(PokemonType.BUG, PokemonType.FIRE, 0.5f);
		setEffectiveness(PokemonType.BUG, PokemonType.GRASS, 2.0f);
		setEffectiveness(PokemonType.BUG, PokemonType.FIGHTING, 0.5f);
		setEffectiveness(PokemonType.BUG, PokemonType.POISON, 0.5f);
		setEffectiveness(PokemonType.BUG, PokemonType.FLYING, 0.5f);
		setEffectiveness(PokemonType.BUG, PokemonType.PSYCHIC, 2.0f);
		setEffectiveness(PokemonType.BUG, PokemonType.GHOST, 0.5f);
		setEffectiveness(PokemonType.BUG, PokemonType.DARK, 2.0f);
		setEffectiveness(PokemonType.BUG, PokemonType.STEEL, 0.5f);

		// ROCK
		setEffectiveness(PokemonType.ROCK, PokemonType.FIRE, 2.0f);
		setEffectiveness(PokemonType.ROCK, PokemonType.ICE, 2.0f);
		setEffectiveness(PokemonType.ROCK, PokemonType.FIGHTING, 0.5f);
		setEffectiveness(PokemonType.ROCK, PokemonType.GROUND, 0.5f);
		setEffectiveness(PokemonType.ROCK, PokemonType.FLYING, 2.0f);
		setEffectiveness(PokemonType.ROCK, PokemonType.BUG, 2.0f);
		setEffectiveness(PokemonType.ROCK, PokemonType.STEEL, 0.5f);

		// GHOST
		setEffectiveness(PokemonType.GHOST, PokemonType.NORMAL, 0.0f);
		setEffectiveness(PokemonType.GHOST, PokemonType.PSYCHIC, 2.0f);
		setEffectiveness(PokemonType.GHOST, PokemonType.GHOST, 2.0f);
		setEffectiveness(PokemonType.GHOST, PokemonType.DARK, 0.5f);
		setEffectiveness(PokemonType.GHOST, PokemonType.STEEL, 0.5f); // Gen 3: Steel resiste Ghost

		// DRAGON
		setEffectiveness(PokemonType.DRAGON, PokemonType.DRAGON, 2.0f);
		setEffectiveness(PokemonType.DRAGON, PokemonType.STEEL, 0.5f);

		// DARK
		setEffectiveness(PokemonType.DARK, PokemonType.FIGHTING, 0.5f);
		setEffectiveness(PokemonType.DARK, PokemonType.PSYCHIC, 2.0f);
		setEffectiveness(PokemonType.DARK, PokemonType.GHOST, 2.0f);
		setEffectiveness(PokemonType.DARK, PokemonType.DARK, 0.5f);
		setEffectiveness(PokemonType.DARK, PokemonType.STEEL, 0.5f); // Gen 3: Steel resiste Dark

		// STEEL
		setEffectiveness(PokemonType.STEEL, PokemonType.FIRE, 0.5f);
		setEffectiveness(PokemonType.STEEL, PokemonType.WATER, 0.5f);
		setEffectiveness(PokemonType.STEEL, PokemonType.ELECTRIC, 0.5f);
		setEffectiveness(PokemonType.STEEL, PokemonType.ICE, 2.0f);
		setEffectiveness(PokemonType.STEEL, PokemonType.ROCK, 2.0f);
		setEffectiveness(PokemonType.STEEL, PokemonType.STEEL, 0.5f);
	}

	private static void setEffectiveness(PokemonType attackType, PokemonType defenseType, float multiplier) {
		chart.get(attackType).put(defenseType, multiplier);
	}

	public static float getEffectiveness(PokemonType attackType, PokemonType defenseType1, PokemonType defenseType2) {
		if (attackType == null || defenseType1 == null)
			return 1.0f;

		float effectiveness = chart.get(attackType).get(defenseType1);
		if (defenseType2 != null) {
			effectiveness *= chart.get(attackType).get(defenseType2);
		}
		return effectiveness;
	}
}