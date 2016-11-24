package br.com.zalem.ymir.client.android.entity.data;

/**
 * Denota a violação de um relacionamento, onde registros das entidades fontes ficariam inválidos se o registro alvo fosse excluído.
 */
public final class RelationshipViolationException extends Exception {

    private final IEntityRecord targetRecord;
    private final String[] sourceEntities;

    public RelationshipViolationException(IEntityRecord targetRecord, String[] sourceEntities) {
        this.targetRecord = targetRecord;
        this.sourceEntities = sourceEntities;
    }

    public IEntityRecord getTargetRecord() {
        return targetRecord;
    }

    public String[] getSourceEntities() {
        return sourceEntities;
    }
}
