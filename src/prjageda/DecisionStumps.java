package prjageda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import weka.core.Instances;

public class DecisionStumps {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private static final int profundidade = 5;
    private static final int quantidade = 10;
    private static final int geracoes = 10;

    private HashMap<String, ArrayList<Atributos>> arvores;

    public DecisionStumps() {
        this.arvores = new HashMap<>();

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="Métodos Get´s e Set´s da classe">           
    public HashMap<String, ArrayList<Atributos>> getArvores() {
        return arvores;
    }

    public void setArvores(HashMap<String, ArrayList<Atributos>> arvore) {
        this.arvores = arvore;

    }

    //</editor-fold>   
    //<editor-fold defaultstate="collapsed" desc="Métodos de Processamento">
    public void ProcessamentoDS(Instances dados) throws Exception {
        try {
            //Declaração Variáveis e Objetos
            ArrayList<Arvores> nodos = new ArrayList<>(), arvores = new ArrayList<>();
            Processamento proc = new Processamento();
            boolean solucao = false;
            int geracao = 1, nivel = 1;

            /*
             Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
             ------------------------------------------------------------------------------------------------------------------
             Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias
             por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
             */
            for (int i = 0; i < dados.numAttributes() - 1; i++) {
                /*
                 1° Passo - Processar todos os Atributos (Binários e Nominais)
                 - 1° Parâmetro - Nome do atributo
                 - 2° Parâmetro - Instâncias e a posição do Atributo
                 */
                nodos.add(new Arvores(dados.instance(0).attribute(i).name(), proc.ProcessamentoInstancias(dados, i)));
            }

            /*
             Geração da População Inicial
             -----------------------------------------------------------------------------------------------------------------
             - Cada Individuo da população será uma Árvore c/ Sub-Árvores
             - Esta possibilidade será para cada um dos atributos existentes
             */
            for (int i = 0; i < quantidade; i++) {
                /*
                 Adicionar o Indivíduo - Árvore Gerada c/ Sub-Arvores, aonde o nível Pai é selecionado aleatóriamente
                
                 Definição dos Parâmetros
                 -------------------------------------------------------------------------------------------------------------
                 1° Parâmetro - Nivel atual de profundidade da Árvore
                 2° Parâmetro - Lista de Nodos Disponíveis para Sorteo
                 3° Parâmetro - Nodo Sorteado p/ ser o Pai
                 */
                arvores.add(GerarPopulacaoIndividuos(nivel, nodos, nodos.get(new Random().nextInt(dados.numAttributes() - 2))));

            }

            //loop até o critério de parada
            while (!solucao && geracao < geracoes) {
                geracao++;

                //Cria nova populacao, utilizando o elitismo
                arvores = proc.NovaGeracaoArvores(dados, arvores, true);

                //verifica se tem a solucao
                solucao = proc.AvaliarSolucao(arvores);

            }

            //Efetuar o processamento das árvores 
            //new Processamento().NovaGeracaoArvores(dados, arvores);
        } catch (Exception e) {
            throw e;

        }

    }

    /*
     Definições sobre o processamento
     --------------------------------------------------------------------------------------------------------------------------
     1° Passo - Sortear uma árvore para ser o nodo raiz e remover da lista (validar com o professor se será outro critério)
     2° Passo - Sortear as demais árvores removendo da Lista Atual
     - Critério de avaliação = Valor % para a substituição do nodo ou não.
     */
    public Arvores GerarPopulacaoIndividuos(int prof, ArrayList<Arvores> nodos, Arvores arvore) {
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
                    arvore.setNodoApartirAresta(i, nodos.get(new Random().nextInt(nodos.size())));

                    //Chamada Recursiva para Geração da árvore atualizando o nivel de profundidade
                    GerarPopulacaoIndividuos(prof + 1, nodos, arvore.getArvoreApartirAresta(i));
                }

            }

        }
        //Definir o retorno
        return arvore;

    }
    //</editor-fold> 

}
