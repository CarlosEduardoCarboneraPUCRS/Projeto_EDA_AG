package prjageda;

import java.util.ArrayList;
import java.util.Random;
import weka.core.Instances;

public class DecisionStumps {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private static final int profundidade = 2;
    private static final int quantidade = 2;
    private static final int geracoes = 10;

    public DecisionStumps() {
    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="Métodos de Processamento">
    public void ProcessamentoDS(Instances dados) throws Exception {
        try {
            //Declaração Objetos
            ArrayList<Arvores> nodos = new ArrayList<>();
            ArrayList<Arvores> arvores = new ArrayList<>();
            Processamento proc = new Processamento();
            int geracao = 1;

            /*
             Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
             ---------------------------------------------------------------------------------------------------------------------------------------------------
             Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias
             por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
             */
            for (int i = 0; i < dados.numAttributes(); i++) {
                /*
                 1° Passo - Processar todos os Atributos (Binários e Nominais)
                 - 1° Parâmetro - Nome do atributo
                 - 2° Parâmetro - Instâncias e a posição do Atributo
                 */
                nodos.add(new Arvores(dados.instance(0).attribute(i).name(), 1, proc.ProcessamentoInstancias(dados, i)));

            }

            /*
             Geração da População Inicial
             ---------------------------------------------------------------------------------------------------------------------------------------------------
             - Cada Individuo da população será uma Árvore c/ Sub-Árvores
             - Esta possibilidade será para cada um dos atributos existentes
             */
            for (int i = 0; i < quantidade; i++) {
                /*
                 Adicionar o Indivíduo - Árvore Gerada c/ Sub-Arvores, aonde o nível Pai é selecionado aleatóriamente
                
                 Definição dos Parâmetros
                 -----------------------------------------------------------------------------------------------------------------------------------------------
                 1° Parâmetro - Nivel atual de profundidade da Árvore
                 2° Parâmetro - Lista de Nodos Disponíveis para Sorteio
                 3° Parâmetro - Nodo Sorteado p/ ser o Nodo Raiz
                 */
                //Declaração Objetos
                Arvores arvore = nodos.get(new Random().nextInt(dados.numAttributes() - 1));

                //Chamada da função para a geração dos indivíduos
                GerarPopulacaoIndividuos(geracao, nodos, arvore);

                //Adicionar o Indivíduo novo
                arvores.add(i, arvore);

            }

            //Laço até o critério de parada ser atingido
            while (geracao < geracoes) {
                //Atualizar o número de Gerações
                geracao++;

                //Cria nova populacao, utilizando o elitismo
                arvores = proc.NovaGeracaoArvores(dados, arvores, true);

            }

        } catch (Exception e) {
            throw e;

        }

    }

    //<editor-fold defaultstate="collapsed" desc="Função Comentada de Recursidade para geração dos Indivíduos">    
    public void GerarPopulacaoIndividuos(int prof, ArrayList<Arvores> nodos, Arvores arvore) {
        //Condição de Parada, Se o grau de profundidade for igual ao informado
        if (prof <= profundidade) {
            //percorrer todas as arestas do indivíduo
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Processar Sim ou Não { Inserir Sub-Árvore } - c/ 50% de Probabilidade 
                if (new Random().nextBoolean()) {
                    /*
                     1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                     2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                     */
                    arvore.SetNodo(arvore.getArestas(i), nodos.get(new Random().nextInt(nodos.size())));

                    //Chamada Recursiva para Geração da árvore atualizando o nivel de profundidade
                    GerarPopulacaoIndividuos(prof + 1, nodos, arvore.getArvoreApartirAresta(i));

                }

            }

        }

    }

    //</editor-fold> 
}
