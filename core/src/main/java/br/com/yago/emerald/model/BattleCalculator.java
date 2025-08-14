package br.com.yago.emerald.model;

import com.badlogic.gdx.math.MathUtils;

public class BattleCalculator {

    /**
     * NOVO: Uma classe interna para retornar múltiplos resultados do cálculo de dano,
     * incluindo o dano, se foi crítico e a efetividade.
     */
    public static class DamageResult {
        public final int damage;
        public final boolean isCritical;
        public final float effectiveness;

        public DamageResult(int damage, boolean isCritical, float effectiveness) {
            this.damage = damage;
            this.isCritical = isCritical;
            this.effectiveness = effectiveness;
        }
    }

    /**
     * MÉTODO ATUALIZADO: Agora retorna um objeto DamageResult completo.
     */
    public static DamageResult calculateDamage(Pokemon attacker, Pokemon defender, Move move) {
        if (move.getCategory() == MoveCategory.STATUS) {
            return new DamageResult(0, false, 1.0f);
        }

        int attackStat;
        int defenseStat;
        if (move.getCategory() == MoveCategory.PHYSICAL) {
            attackStat = attacker.getAttack();
            defenseStat = defender.getDefense();
        } else { // SPECIAL
            attackStat = attacker.getSpecialAttack();
            defenseStat = defender.getSpecialDefense();
        }

        float damage = (((((attacker.getLevel() * 2f / 5f) + 2f) * move.getPower() * attackStat / defenseStat) / 50f) + 2f);

        float stab = 1.0f;
        if (move.getType() == attacker.getType1() || move.getType() == attacker.getType2()) {
            stab = 1.5f;
        }

        float typeEffectiveness = TypeChart.getEffectiveness(move.getType(), defender.getType1(), defender.getType2());

        float randomFactor = MathUtils.random(0.85f, 1.0f);
        
        boolean isCritical = MathUtils.random(1, 16) == 1;
        float critical = isCritical ? 2.0f : 1.0f;

        float modifier = stab * typeEffectiveness * critical * randomFactor;
        damage *= modifier;

        int finalDamage = (int) damage;
        if (finalDamage < 1 && typeEffectiveness > 0) {
            finalDamage = 1;
        }
        
        return new DamageResult(finalDamage, isCritical, typeEffectiveness);
    }
}