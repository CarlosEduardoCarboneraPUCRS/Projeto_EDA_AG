package prjageda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Processamento {

    //<editor-fold defaultstate="collapsed" desc="Declaração Atributos e Método(s) Construtor(es) da Classe">    
    private String caminhoDados;
    private Arvores arvTemporaria = null;
    private ArrayList<String> nomesAtrs = null;

    //Declaração de objetos
    public static final MersenneTwister mt = new MersenneTwister();

    //Configuração para mutação da árvore
    private static final double percMutacao = 0.05; //5% de Possibilidade de Mutação
    private static final int qtdSelTorneio = 10;

    public String getCaminhoDados() {
        return caminhoDados;
    }

    public void setCaminhoDados(String caminho) {
        this.caminhoDados = caminho;
    }

    public Processamento(String local) {
        //Inicialização dos atributos
        this.caminhoDados = local;

    }

    public Processamento() {

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Métodos de Processamento Diversos">        
    //Leitura do Arquivo
    public Instances LeituraArquivo() {
        //Declaração Variáveis e Objetos
        Instances dados = null;

        try {
            //Inicialização da Leitura
            dados = new DataSource(caminhoDados).getDataSet();

            //Setar o atributo classe
            if (dados.classIndex() == -1) {
                dados.setClassIndex(dados.numAttributes() - 1);
            }

        } catch (Exception e) {
            //Se ocorreu alguma exceção
            System.out.println(e.getMessage());

        }

        //Definição do Retorno
        return dados;

    }

    //Processamento das instâncias lidas da base de dados
    public ArrayList<Atributos> ProcessamentoInstancias(Instances dados, int posicao) {
        //Declaração Variáveis e Objetos
        ArrayList<Atributos> registros = new ArrayList<>();

        //1 - Avaliar se o Atributo é Numérico ou Nominal 
        //  - 1.1 - Se Numérico a árvore terá 2 arestas (Árvore será bifurcada) 
        //  - 1.2 - Se Categórico terá o número de arestas em função da quantidade de atributos encontrados no dataset
        if (dados.attribute(posicao).isNumeric()) {
            //Declaração Variáveis e Objetos
            String indiceGini = String.valueOf(Arredondar(calcularIndiceGini(dados, posicao), AlGEnArDe.qtdDecimais, 1));

            //Para atributo numéricos, SEMPRE será bifurcada, assim: 
            // Aresta 0 - Sempre será MENOR OU IGUAL a Média Calculada
            // Aresta 1 - Sempre será MAIOR que a Média Calculada
            registros.add(new Atributos("<= " + indiceGini, null, "", null));
            registros.add(new Atributos("> " + indiceGini, null, "", null));

        } else {
            //Percorrer a Quantidade de Atributos existentes e adicionando os mesmos
            for (int i = 0; i < dados.attribute(posicao).numValues(); i++) {
                //Adicionar as Arestas
                registros.add(new Atributos(dados.attribute(posicao).value(i), null, "", null));

            }

        }

        //Definir o retorno
        return registros;

    }

    //Efetuar o processamento Recursivo da Árvore, lendo cada instância da base de dados e percorrer toda a árvore
    public ArrayList<Arvores> NovaGeracaoArvores(Instances dados, ArrayList<Arvores> popArvores, boolean elitismo) throws IOException, Exception {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> populacao = new ArrayList<>();
        ArrayList<Arvores> filhos;
        //------------------------------------------------------------------------------------------------------------------------------------------------------------------------//
        //Efetuar o DeepCopy das Árvores(JÁ QUE TUDO NO JAVA É POR REFERÊNCIA)                                                                                                    //  
        //                                                                                                                                                                        //
        List<Arvores> listagem = ObjectUtil.deepCopyList(popArvores);                                                                                                             //  
        //                                                                                                                                                                        //
        //------------------------------------------------------------------------------------------------------------------------------------------------------------------------//

        //Se tiver elitismo, adicionar (mantém) a melhor árvore da geração atual(ordenada) para a próxima geração
        if (elitismo) {
            //Adicionar as árvores obtidas por Elitismo (Clonar os Objetos - Cópias Profundas)
            populacao.add((Arvores) listagem.get(0));
            populacao.add((Arvores) listagem.get(1));

        }

        //Efetua a geração da nova população equanto a população for menor que a população inicialmente estabelecida
        while (populacao.size() < AlGEnArDe.quantidade) {
            //Inicialização do Objeto
            filhos = new ArrayList<>();

            //Adicionar os pais
            filhos.add((Arvores) SelecaoPorTorneio(listagem));
            filhos.add((Arvores) SelecaoPorTorneio(listagem));

            //SE Valor Gerado <= TxCrossover, realiza o Crossover entre os pais SENÃO mantém os pais selecionados através de Torneio p/ a próxima geração            
            if (mt.nextDouble() <= AlGEnArDe.TxCrossover) {
                //Adicionar os 2 filhos que sofreram Crossover
                populacao.addAll(CrossoverArvores(filhos.get(0), filhos.get(1)));

            } else {
                //Apenas adicionar os 2 filhos selecionados
                populacao.add(filhos.get(0));
                populacao.add(filhos.get(1));

            }

        }

        //Efetuar Mutação das Árvores (se selecionado pelo critério do %), Exceto p/ as Árvores obtidas por Eletismo
        for (int i = 2; i < populacao.size(); i++) {
            //Se for MENOR OU IGUAL ao limite Superior (Valor >= 0 E Valor <= Limite Superior)
            if (Arredondar(mt.nextDouble(), 2, 1) < Processamento.percMutacao) {
                //Declaração Variáveis e Objetos
                String atributo = BuscarAtributosArvore(populacao.get(i));

                //Caso a árvore possuir mais do que o nodo raiz, retornará um atributo, caso contrário nem processa
                if (!atributo.isEmpty()) {
                    //Efetuar a Mutação da Árvore - "E"xpansão ou "R"etração de Nodos
                    MutacaoArvores(populacao.get(i), mt.nextBoolean() ? "E" : "R", dados, atributo);

                }

            }

        }

        //Definição do retorno da função
        return populacao;

    }

    //Efetuar a seleção por Torneio das Árvores - Seleciona-se as Árvores Aleatóriamente Ordenando-os Crescente
    private Arvores SelecaoPorTorneio(List<Arvores> arvores) throws Exception {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> selecao = new ArrayList<>();

        //Selecionar 2 árvores aleatóriamente
        selecao.add(arvores.get(mt.nextInt(arvores.size() - 1)));
        selecao.add(arvores.get(mt.nextInt(arvores.size() - 1)));

        //Retornar a melhor arvore (Clonar o Objeto - Cópia Profunda)
        return ((Arvores) (selecao.get(0).getFitness() < selecao.get(1).getFitness() ? selecao.get(0) : selecao.get(1)));
    }

    //Efetuar o crossover da população de árvores, aonde ocorre a Troca Genética de Material entre as árvores CRIANDO novas árvores
    private ArrayList<Arvores> CrossoverArvores(Arvores arv1, Arvores arv2) {
        //Declaração Variáveis e Objetos e Inicializações
        ArrayList<Arvores> populacao = new ArrayList<>();

        try {
            //Declaração Variáveis e Objetos
            //Deverá ser avaliado a opção de que as 2 Arvores devm possuir nodos ALÉM do nodo raiz, se possuir processa caso contrário não
            String atributoArv1 = BuscarAtributosArvore(arv1);
            String atributoArv2 = BuscarAtributosArvore(arv2);

            //Se o atributo for válido processa SENÃO abandona o processamento
            if (!atributoArv1.isEmpty()) {
                //Buscar um dos Atributos Selecionados Aleatóriamente e Remove a Sub-Árvore da 1° Árvore Transformando em um Nodo "Folha" na posição 
                RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arv1, atributoArv1);

                //Se o atributo for válido processa SENÃO abandona o processamento
                if (!atributoArv2.isEmpty()) {
                    //Incluir o nodo removido na 1° Árvore em uma posição aleatório da 2° Árvore (desde que a posição seja um nodo folha)
                    PesquisarPosicaoArvoreDestino(arv2);

                }

            }

            //Se o atributo for válido processa SENÃO abandona o processamento
            if (!atributoArv2.isEmpty()) {
                //Buscar um dos atributos selecionados aleatóriamente e Remover Sub-Árvore da 2° Árvore transformando em um nodo folha na respectiva posição 
                RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arv2, atributoArv2);

                //Se o atributo for válido processa SENÃO abandona o processamento
                if (!atributoArv1.isEmpty()) {
                    //Incluir o nodo removido na 1° Árvore em uma posição aleatória da 2° Árvore (desde que a posição seja um nodo folha)
                    PesquisarPosicaoArvoreDestino(arv1);

                }

            }

            //Adicionar as árvores c/ o "CROSSOVER" efetuado e limpar o objeto
            populacao.add(arv1);
            populacao.add(arv2);

            //Liberar o Objeto
            arvTemporaria = null;

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

        //Definir o retorno
        return populacao;

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Funções Pertinentes aos Métodos de Mutação">   
    //Efetuar a Mutação da árvore, a mesma poderá ser de "E"xpansão ou "R"edução
    private void MutacaoArvores(Arvores arvore, String tipo, Instances dados, String atributo) throws IOException {
        //SE for "E" - EXPANSÃO - Vai até um nodo FOLHA ALEATÓRIO E ADICIONA um AlGEnArDe Aleatóriamente
        //SENÃO  "R" - REDUÇÃO  - Vai até o nodo passado como parâmetro e transforma-se todos as sub-árvores abaixo em folhas
        if (tipo.equals("E")) {
            //Se possuir arestas válidas
            if (arvore.getArestas() != null) {
                //Declaração Variáveis e Objetos - Selecionar uma posição aleatória
                int posicao = arvore.getArestas().size() == 1 ? 0 : mt.nextInt(arvore.getArestas().size() - 1);

                //Se a aresta selecionada não for nula
                if (arvore.getArestas(posicao) != null) {
                    //Se a aresta selecionada não for nula
                    if (arvore.getArestas(posicao).getNodo() != null) {
                        //Se atingiu o MAIOR NÍVEL de profundidade da árvore (O último nodo da aresta DEVERÁ SER nulo) - Avaliando a aresta SELECIONADA aleatóriamente
                        if (arvore.getArestas(posicao).getNodo().getArestas(posicao).getNodo() == null) {
                            //Declaração Variáveis e Objetos
                            ArrayList<Arvores> temp = new AlGEnArDe().LeituraNodos();

                            //Selecionar aleatóriamente uma árvore p/ ser incluida no nodo raiz e Adicionar o nodo na aresta selecionada
                            arvore.getArestas(posicao).getNodo().getArestas(posicao).setNodo(temp.get(Processamento.mt.nextInt(temp.size() - 1)));

                        } else {
                            //Chamar a função recursivamente até chegar em um nodo raiz
                            MutacaoArvores(arvore.getArestas(posicao).getNodo(), tipo, dados, atributo);

                        }

                    } else {
                        //Declaração Variáveis e Objetos
                        ArrayList<Arvores> temp = new AlGEnArDe().LeituraNodos();

                        //Se o nodo for nulo, seleciona uma Decision Stump aleatóriamente p/ ser incluida no nodo folha
                        arvore.getArestas(posicao).setNodo(temp.get(Processamento.mt.nextInt(temp.size() - 1)));

                    }

                }

            }

        } else {
            //Se possuir arestas válidas
            if (arvore.getArestas() != null) {
                //Percorrer todas as arestas
                for (int i = 0; i < arvore.getArestas().size(); i++) {
                    //Se o nodo não for nulo
                    if (arvore.getArestas(i).getNodo() != null) {
                        //Se for o Atributo Selecionado Atribuo nulo senão retorno pra pesquisa
                        if (arvore.getArestas(i).getNodo().getNomeAtributo().equals(atributo)) {
                            //Transformar o Nodo c/ arestas em Nodo Folha (Mutação de "REDUÇÃO") E Sair do processamento
                            arvore.getArestas(i).setNodo(null);
                            break;

                        } else {
                            //Chamada recursiva da função atualizando o nível de profundidade
                            MutacaoArvores(arvore.getArestas(i).getNodo(), tipo, dados, atributo);

                        }

                    }

                }

            }

        }

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Funções Destinadas ao Crossover">
    //Remover uma Sub-Árvore da Árvore atual e setar nulo a mesma
    public void RemoverNodoNaOrigemSetandoNuloERetornandoArvore(Arvores arvore, String atributo) {
        try {
            //Se as arestas não forem nulas
            if (arvore.getArestas() != null) {
                //Se possuir arestas
                if (arvore.getArestas().size() > 0) {
                    //Percorre todas as arestas até encontrar o atributos selecionado
                    for (int i = 0; i < arvore.getArestas().size(); i++) {
                        //Se a aresta selecionada não for nula
                        if (arvore.getArestas(i) != null) {
                            //Se o nodo não for nulo
                            if (arvore.getArestas(i).getNodo() != null) {
                                //Se o nome do atributoi não for nulo
                                if (arvore.getArestas(i).getNodo().getNomeAtributo() != null) {
                                    //Se for o nodo da aresta selecionado aleatóriamente
                                    if (arvore.getArestas(i).getNodo().getNomeAtributo().equals(atributo)) {
                                        //Criar o novo objeto e atribuir o mesmo
                                        arvTemporaria = new Arvores();
                                        arvTemporaria = arvore.getArestas(i).getNodo();

                                        //Setar nulo p/ a sub-árvore selecionada
                                        arvore.getArestas(i).setNodo(null);
                                        arvore.getArestas(i).setClasses(null);
                                        arvore.getArestas(i).setClasseDominante("");

                                        //Sair do for 
                                        break;

                                    }

                                } else {
                                    //Chamar Recursivamente a função até encontrar o nodo
                                    RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arvore.getArestas(i).getNodo(), atributo);

                                }

                            }

                        }

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Pesquisar um nodo folha da árvore de destino p/ inserção da Sub-Árvore
    public void PesquisarPosicaoArvoreDestino(Arvores arvore) {
        try {
            //Se possuir arestas
            if (arvore.getArestas() != null) {
                //Selecionar uma posição aleatóriamente (Não importa qual, pois é inclusão de um novo nodo)
                int posicao = arvore.getArestas().size() == 1 ? 0 : mt.nextInt(arvore.getArestas().size() - 1);

                //Se a aresta selecionada não for nula
                if (arvore.getArestas(posicao) != null) {
                    //Se o Nodo da Aresta não for nulo, existem Sub-Árvores
                    if (arvore.getArestas(posicao).getNodo() != null) {
                        //Chamada Recursiva da Função p/ Avaliação da Sub-Árvore informada
                        PesquisarPosicaoArvoreDestino(arvore.getArestas(posicao).getNodo());

                    } else {
                        //Se a aresta informada for nula insere o nodo e finaliza o ciclo
                        arvore.getArestas(posicao).setNodo(arvTemporaria);

                        //Setar as propriedades
                        arvore.getArestas(posicao).setClasses(null);
                        arvore.getArestas(posicao).setClasseDominante("");

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Atribui ao nodo folha a(s) classe(s) a qual pertence, se não existe atribui a mesma senão atualiza a quantidade em um unidade(caso exista a mesma)
    public void AtribuicaoClasseNodosFolhas(Arvores arvore, Instance avaliacao) {
        //Se o árvore não for nula
        if (arvore != null) {
            //Percorrer todos os atributos da instância selecionada (Exceto o atributo Classe)
            for (int k = 0; k < avaliacao.numAttributes(); k++) {
                //Se o nome do Atributo Classe for igual ao nome do atributo da instância (Raiz ou nodo folha)
                if (avaliacao.attribute(k).name().equals(arvore.getNomeAtributo())) {
                    //Se o atributo for Numérico (BIFURCAÇÃO)
                    if (avaliacao.attribute(k).isNumeric()) {
                        //Se a aresta não for nula
                        if (arvore.getArestas() != null) {
                            //Declaração Variáveis e Objetos
                            double valorAresta = Double.valueOf(arvore.getArestas(0).getAtributo().split(" ")[1]);

                            //Se valor posição 0 FOR MENOR IGUAL ao valor atributo selecionado (Então posição igual a 0 SENAO 1)
                            int posicao = Arredondar(avaliacao.value(k), AlGEnArDe.qtdDecimais, 1) <= Arredondar(valorAresta, AlGEnArDe.qtdDecimais, 1) ? 0 : 1;

                            //Se não for um nodo RAIZ, efetua a chamada recursiva da função até chegar em um nodo raiz
                            if (arvore.getArestas(posicao).getNodo() != null) {
                                //Chama a função recursivamente passando o nodo da aresta
                                AtribuicaoClasseNodosFolhas(arvore.getArestas(posicao).getNodo(), avaliacao);

                            } else {
                                //Declaração Variáveis e Objetos
                                ArrayList<Classes> classes = new ArrayList<>();

                                //Se a Classe for vazia Inclui o mesmo (Sendo o 1° Registro)
                                if (arvore.getArestas(posicao).getClasses() == null) {
                                    //Adicionar a Nova classe 
                                    classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                    //Atribuir as classes e sair fora da execução para a aresta selecionada
                                    arvore.getArestas(posicao).setClasses(classes);

                                } else //Já Existem Registros na Classe, irá atualizar o mesmo
                                {
                                    //Declaração Variáveis e Objetos
                                    boolean bOk = false;
                                    classes = arvore.getArestas(posicao).getClasses();

                                    //Percorre TODAS as classes do Nodo
                                    for (Classes classe : classes) {
                                        //Se o valor da aresta FOR IGUAL AO VALOR DO ATRIBUTO DA INSTÂNCIA                                            
                                        //Se o "VALOR" da classe DA INSTÂNCIA SELECIONADA FOR IGUAL a da classe informada atualiza a quantidade
                                        if (avaliacao.classAttribute().value((int) avaliacao.classValue()).equals(classe.getNome())) {
                                            //Atualizar a quantidade (Adicionando 1) de registros X Atributo - Para Definir a Classe dominante
                                            classe.atualizarQtd(1);
                                            bOk = true;

                                        }

                                    }

                                    //Se não existe o atributo inclui o mesmo
                                    if (!bOk) {
                                        //Se não for nulo (ser o último)
                                        if (avaliacao.classAttribute() != null) {
                                            //Adicionar a Nova classe e atualizar a quantidade
                                            classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                        }

                                    }
                                    //Atribuir as classes e sair fora da execução para a aresta selecionada
                                    arvore.getArestas(posicao).setClasses(classes);

                                }

                            }

                        }

                    } else { //Se for categórico
                        if (arvore.getArestas() != null) {
                            //Percorrer todas as arestas da arvore
                            for (int i = 0; i < arvore.getArestas().size(); i++) {
                                //Declaração Variáveis e Objetos
                                ArrayList<Classes> classes = arvore.getArestas(i).getClasses();

                                //Percorrer todas as classes da Aresta
                                for (Classes classe : classes) {
                                    //Percorrer todos os valores existentes da instância selecionada
                                    for (int l = 0; l < avaliacao.numValues(); l++) {
                                        //Se o nome do Atributo FOR IGUAL AO NOME DO ATRIBUTO da instancia de avaliação selecionada
                                        if (arvore.getArestas(i).getAtributo().equals(avaliacao.attribute(k).value(l))) {
                                            //Se o nome da classe dominante for igual a classe avaliada
                                            if (classe.getNome().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                                                //Atualizar a quantidade de registros X Atributo - Para Definir a Classe dominante
                                                classe.atualizarQtd(1);

                                            }

                                        }

                                    }

                                }

                            }
                        }

                    }
                    //Sair fora do for
                    break;

                }

            }

        }

    }

    //Irá percorrer todos os Nodos da árvore(avaliando SOMENTE os nodos folhas) 
    public void DefinicaoClasseMajoritariaNodosFolhas(Arvores arvore) {
        //Se o árvore não for nula
        if (arvore.getArestas() != null) {
            //Percorrer TODAS as arestas do árvore selecionado para atribuir uma classe as folhas
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Se a aresta selecionada não for NULA pesquisa pela mesma (NULA == Nodo Folha)
                if (arvore.getArestas(i).getNodo() != null) {
                    //Chamada recursiva da função passando como parâmetros a aresta selecionada
                    DefinicaoClasseMajoritariaNodosFolhas(arvore.getArestas(i).getNodo());

                } else //Chegou em um nodo folha
                {
                    //Declaração Variáveis e Objetos
                    ArrayList<Classes> classes = arvore.getArestas(i).getClasses();

                    //Se não for nulo
                    if (classes != null) {
                        //Declaração Variáveis e Objetos
                        String NmClasseMaj = "";
                        double qtdClasse = 0;

                        //Percorre todas as Classes
                        for (Classes classe : classes) {
                            //Se for a 1° Ocorrência
                            if (NmClasseMaj.isEmpty()) {
                                //Atribuições do nome da classe e da quantidade
                                NmClasseMaj = classe.getNome();
                                qtdClasse = classe.getQuantidade();

                            } else {
                                //Se a quantidade for MAIOR que a ATUAL ALTERA a classe SENÃO mantém a mesma
                                if (classe.getQuantidade() > qtdClasse) {
                                    //Atribuições do nome da classe e da quantidade
                                    NmClasseMaj = classe.getNome();
                                    qtdClasse = classe.getQuantidade();

                                }

                            }

                        }

                        //Setar a classe Majoritária
                        arvore.getArestas(i).setClasseDominante(NmClasseMaj);

                    }

                }

            }

        }

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Calcular o Valor Médio da Árvores(arestas) - Para Atributos Numéricos">    
    //Efetuar o Cálculo do Indice Gini p/ Atributos Contínuos 
    //Por Exemplo Indice Gini = 1 - (Somatório Quant. Atrib. "A" / Total de Instâncias) ^ 2 - (Somatório Quant. Atrib. "N" / Total de Instâncias) ^ 2.
    private double calcularIndiceGini(Instances dados, int pos) {
        //Declaração Variáveis e objetos
        List<IndiceGini> valores = new ArrayList<>();
        List<Double> indiceGini = new ArrayList<>();

        /**
         ** Creating Filter Logic
         *
         */
        Filter<IndiceGini, Double> filter = new Filter<IndiceGini, Double>() {
            @Override
            public boolean isMatched(IndiceGini object, Double valor) {
                return object.getValor() == valor;

            }
        };

        //Pegar distintamente os valores p/ calcular a média de todas as instâncias do atributo informado        
        //Adicionar os valores das instâncias na posição informada
        for (int i = 0; i < dados.numInstances(); i++) {
            //Se não contiver o valor Insere SENÃO Atualiza a quantidade
            if (valores.isEmpty()) {
                //Incluir o Valor
                valores.add(new IndiceGini(dados.instance(i).value(pos), 1));

            } else {
                List<IndiceGini> filtrados = new FilterList().filterList(valores, filter, dados.instance(i).value(pos));

                if (filtrados.isEmpty()) //Adicionar 1 na quantidade de incidências e sair fora do loop
                {
                    filtrados.get(0).adicionar(1);

                } else {
                    //Incluir o Valor
                    filtrados.add(new IndiceGini(dados.instance(i).value(pos), 1));

                }

            }

        }

        //Ordenar Crescente
        Collections.sort(valores);
       
        //Ordenar Crescente
        Collections.sort(indiceGini);

        //Definir o Retorno (se o Índice Gini for "exatamente" 1 deverá ser 
        return Arredondar(indiceGini.get(0) == 1 ? 0 : indiceGini.get(0), AlGEnArDe.qtdDecimais, 1);

    }

    // Parâmetros: 1 - Valor a Arredondar. 
    //             2 - Quantidade de casas depois da vírgula. 
    //             3 - Arredondar para cima ou para baixo?
    // Para Cima  = 0 (ceil) 
    // Para Baixo = 1 ou qualquer outro inteiro (floor)
    double Arredondar(double valor, int casas, int ACimaouABaixo) {
        //Declaração Variáveis e objetos
        double arredondado = valor;

        //Atribuições do Cálculo
        arredondado *= (Math.pow(10, casas));
        arredondado = (ACimaouABaixo == 0 ? Math.ceil(arredondado) : Math.floor(arredondado));
        arredondado /= (Math.pow(10, casas));

        //Definir o Retorno
        return arredondado;

    }

    private String BuscarAtributosArvore(Arvores arv) {
        //Declaração Variáveis e Objetos
        nomesAtrs = new ArrayList<>();

        //Processar os atributos existentes na árvore
        processarNomesAtributos(arv);

        //Se for diferente de nulo(carregou algum atribuito)
        if (!nomesAtrs.isEmpty()) {
            //Se existir apenas o nodo raiz
            if (nomesAtrs.size() > 1) {
                //Não poderá retornar o nodo raiz, por isto a exclusão do nodo na posição 0, sendo assim remove-se o nodo raiz
                nomesAtrs.remove(0);

                //Se o tamanho for 1, pega o único existente (sem sorteio) SENÃO Sortear um entre os possíveis
                return (nomesAtrs.size() == 1) ? nomesAtrs.get(0) : nomesAtrs.get(mt.nextInt(nomesAtrs.size() - 1));

            }

        }

        //Definir o retorno
        return "";

    }

    //Localizar os atributos(nomes) existentes na árvore, aonde será sorteado um deles para mutação (EXCETO o nodo Raiz)
    private void processarNomesAtributos(Arvores arv) {
        try {
            //Se possuir arestas válidas
            if (arv.getArestas() != null) {
                //Percorrer todas as arestas
                for (int i = 0; i < arv.getArestas().size(); i++) {
                    //Se o atributo não for nulo
                    if (arv.getNomeAtributo() != null) {
                        //Se for vazio Adiciona senão avalia e depois insere sim ou não
                        if (nomesAtrs.isEmpty()) {
                            //Adicionar o nome do Atributo SE não Contiver
                            nomesAtrs.add(String.valueOf(arv.getNomeAtributo()));

                        } else {
                            //Se não contiver o atributo
                            if (!nomesAtrs.contains(arv.getNomeAtributo())) {
                                //Adicionar o nome do Atributo SE não Contiver
                                nomesAtrs.add(arv.getNomeAtributo());

                            }
                        }

                        if (arv.getArestas(i) != null) {
                            //Se o nodo não for nulo
                            if (arv.getArestas(i).getNodo() != null) {
                                //Chamada recursiva da árvore
                                processarNomesAtributos(arv.getArestas(i).getNodo());

                            }

                        }

                    } else {
                        System.out.println("Atributo é nulo ");

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getCause());

        }

    }
    //</editor-fold>        

}
