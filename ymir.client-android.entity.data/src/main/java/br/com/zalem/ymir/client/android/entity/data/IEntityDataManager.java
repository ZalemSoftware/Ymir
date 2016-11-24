package br.com.zalem.ymir.client.android.entity.data;

import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.query.IQueryBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.IQueryStatement;
import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;

/**
 * Gerenciador de dados das entidades da aplicação.<br>
 * Possui o registro das entidades suportadas, disponibilizando meios para obter
 * seus metadados através do método {@link #getEntityMetadata(String)} e seus acessores de dados através do método
 * {@link #getEntityDAO(String)}.<br>
 * Também permite a criação de queries a partir do método {@link #query()}.
 *
 * @author Thiago Gesser
 */
public interface IEntityDataManager {
	
	/**
	 * Obtém os metadados da entidade referenciada pelo nome.
	 * 
	 * @param entityName nome da entidade.
	 * @return os metadados da entidade.
	 * @throws IllegalArgumentException se não houver uma entidade registrada com este nome.
	 */
	IEntityMetadata getEntityMetadata(String entityName);
	
	/**
	 * Obtém os metadados das entidades registradas neste gerenciador de entidades.
	 * 
	 * @return os metadados das entidades.
	 */
	IEntityMetadata[] getEntitiesMetadatas();

	
	/**
	 * Obtém o acessor de dados da entidade refernaciada pelo nome.
	 * 
	 * @param entityName nome da entidade.
	 * @return o acessor de dados da entidade.
	 * @throws IllegalArgumentException se não houver uma entidade registrada com este nome.
	 */
	IEntityDAO getEntityDAO(String entityName);
	
	/**
	 * Obtém o acessor de dados da entidade refernaciada pela visão de dados.<br>
	 * O visão baseada em relacionamento do tipo array limita os dados do DAO para o conjunto de registros
	 * referenciados por um registro fonte. 
	 * 
	 * @param dataView visão de dados
	 * @return o acessor de dados da entidade.
	 * @throws IllegalArgumentException se a entidade referenciada pela visão de dados não existir.
	 */
	IEntityDAO getEntityDAO(RelationshipArrayView dataView);

	
	/**
	 * Inicia a construção de uma query com o conceito de <code>Fluent Interface</code>.
	 * 
	 * @return o {@link IQueryStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	IQueryStatement query();

    /**
     * Cria um montador de query que provê todos os métodos de montagem de uma vez.<br>
     * Isto torna a montagem mais livre, mas sem nenhum auxílio contextual sobre o formato correto da query.<br>
     * <br>
     * Recomenda-se utilizar apenas para a montagem de queries que exigem uma lógica diferenciada. Para as demais, o {@link #query()} é mais indicado.
     *
     * @return o montador de queries.
     */
    IQueryBuilder newQueryBuilder();


    /**
     * Exclui todos os registros das entidades especificadas.<br>
     * A operação é executada em background, sendo que o monitor é avisado sobre o início, meio e o fim da operação (na Thread de UI).
     * Também é possível utilizar o monitor para cancelar a operação de exclusão, o que deve reverter qualquer exclusão já realizada.
     * Cancelar a operação após seu término não terá nenhum efeito.
     *
     * @param monitor monitor da operação de exclusão ou <code>null</code> se não é necessario monitorá-la.
     * @param entitiesNames nomes das entidades cujo seus registros serão excluidos.
     */
    void deleteAll(AbstractEntityRecordDeletionMonitor monitor, String... entitiesNames);


    /**
     * Monitor utilizado na exclusão de registros de entidade disparado pelo método {@link #deleteAll(AbstractEntityRecordDeletionMonitor, String...)}.<br>
     * A exclusão dos registros pode ser cancelada através do método {@link #cancel()} até a chamada de {@link #onDeleteRecords()}.<br>
     * Todos os métodos de acompanhamento da exclusão são executados na Thread de UI.
     */
    abstract class AbstractEntityRecordDeletionMonitor {

        private boolean canceled;
        private boolean finishing;

        /**
         * Chamado antes de iniciar a exclusão dos registros das entidades.
         */
        public void beforeDeleteRecords() {
        }

        /**
         * Chamado no momento da efetivação ou cancelamento da exclusão dos registros das entidades. A partir deste momento, o {@link #cancel()}
         * tem mais efeito e o {@link #isFinishing()} retorna <code>true</code>.
         */
        public void onDeleteRecords() {
        }

        /**
         * Chamado após a processo de exclusão dos registros das entidades, mesmo que a exclusão tenha sido cancelada.
         */
        public void afterDeleteRecords() {
        }


        /**
         * Sinaliza que a exclusão dos registros das entidades deve ser cancelada.
         */
        public synchronized final void cancel() {
            canceled = true;
        }

        /**
         * Indica se a exclusão dos registros das entidades foi cancelada.
         *
         * @return <code>true</code> se a exclusão foi cancelada e <code>false</code> caso contrário.
         */
        public synchronized final boolean isCanceled() {
            return canceled;
        }

        /**
         * Indica se o processo de exclusão está terminando e que não pode mais ser cancelado.
         *
         * @return <code>true</code> se o processo de exclusão está terminando e <code>false</code> caso contrário.
         */
        public synchronized boolean isFinishing() {
            return finishing;
        }

        /**
         * Define se o processo de exclusão está terminando.
         *
         * @param finishing <code>true</code> se o processo de exclusão está terminando e <code>false</code> caso contrário.
         */
        public synchronized void setFinishing(boolean finishing) {
            this.finishing = finishing;
        }
    }
}
