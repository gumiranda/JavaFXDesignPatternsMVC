package javafxmvc.controller;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafxmvc.model.dao.ClienteDAO;
import javafxmvc.model.dao.ProdutoDAO;
import javafxmvc.model.database.Database;
import javafxmvc.model.database.DatabaseFactory;
import javafxmvc.model.domain.Cliente;
import javafxmvc.model.domain.DecoratorPresente;
import javafxmvc.model.domain.ComponentItemDeVenda;
import javafxmvc.model.domain.Desconto;
import javafxmvc.model.domain.Desconto10;
import javafxmvc.model.domain.Desconto20;
import javafxmvc.model.domain.Desconto30;
import javafxmvc.model.domain.Desconto40;
import javafxmvc.model.domain.EstadoState;
import javafxmvc.model.domain.EstoqueCritico;
import javafxmvc.model.domain.EstoqueEmFalta;
import javafxmvc.model.domain.EstoqueNormal;
import javafxmvc.model.domain.ItemDeVendaDecorator;
import javafxmvc.model.domain.ItemDeVenda;
import javafxmvc.model.domain.Produto;
import javafxmvc.model.domain.Venda;

public class FXMLAnchorPaneProcessosVendasDialogController implements Initializable {

    @FXML
    private ComboBox comboBoxVendaCliente;
    @FXML
    private DatePicker datePickerVendaData;
    @FXML
    private CheckBox checkBoxVendaPago, checkBoxPresente;
    @FXML
    private ComboBox comboBoxVendaProduto;
    @FXML
    private TableView<ItemDeVenda> tableViewItensDeVenda;
    @FXML
    private TableColumn<ItemDeVenda, Produto> tableColumnItemDeVendaProduto;
    @FXML
    private TableColumn<ItemDeVenda, Integer> tableColumnItemDeVendaQuantidade;
    @FXML
    private TableColumn<ItemDeVenda, Double> tableColumnItemDeVendaValor;
    @FXML
    private TextField textFieldVendaValor;
    @FXML
    private TextField textFieldVendaItemDeVendaQuantidade;
    @FXML
    private Button buttonConfirmar;
    @FXML
    private Button buttonCancelar;
    @FXML
    private Button buttonAdicionar;

    private List<Cliente> listClientes;
    private List<Produto> listProdutos;
    private ObservableList<Cliente> observableListClientes;
    private ObservableList<Produto> observableListProdutos;
    private ObservableList<ItemDeVenda> observableListItensDeVenda;

    //Atributos para manipulação de Banco de Dados
    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    private Stage dialogStage;
    private boolean buttonConfirmarClicked = false;
    private Venda venda;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clienteDAO.setConnection(connection);
        produtoDAO.setConnection(connection);
        carregarComboBoxClientes();
        carregarComboBoxProdutos();
        tableColumnItemDeVendaProduto.setCellValueFactory(new PropertyValueFactory<>("produto"));
        tableColumnItemDeVendaQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        tableColumnItemDeVendaValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
    }

    public void carregarComboBoxClientes() {
        listClientes = clienteDAO.listar();
        observableListClientes = FXCollections.observableArrayList(listClientes);
        comboBoxVendaCliente.setItems(observableListClientes);
    }

    public void carregarComboBoxProdutos() {
        listProdutos = produtoDAO.listar();
        observableListProdutos = FXCollections.observableArrayList(listProdutos);
        comboBoxVendaProduto.setItems(observableListProdutos);
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public Venda getVenda() {
        return this.venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public boolean isButtonConfirmarClicked() {
        return buttonConfirmarClicked;
    }
public void setEstado(int qtd,Produto produto){
     if(qtd > 10)
		{
                           EstadoState  estado = new EstoqueNormal(produto);
produto.setEstado(estado);
		} else if(qtd > 0 )
		{
        EstadoState  estado = new EstoqueCritico(produto);
        produto.setEstado(estado);
		}else if(qtd== 0){
                          EstadoState   estado = new EstoqueEmFalta(produto);
                          produto.setEstado(estado);

                }
}
public boolean verificarNovamenteEstado(Produto produto,int quantidadeItemDeVenda){
        int q = produto.getQuantidade()- quantidadeItemDeVenda;
        if(q>=0){
            setEstado(q,produto);
        }
        else{
            return false;
        }
         return verificaEstado(produto);
}
public boolean verificaEstado(Produto produto){
            EstadoState s = produto.getEstado();
    if(s instanceof EstoqueEmFalta){
        Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Problema!");
                alert.setContentText("Não existe a quantidade de produtos disponíveis no estoque");
                alert.show();
                return false;
    }
    else if(s instanceof EstoqueNormal){
        return true;
    }else if(s instanceof EstoqueCritico){
          Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Atenção!");
                alert.setContentText("O estoque do produto está crítico");
                alert.show();
                return true;
    }
    return true;
}
    @FXML
    public void handleButtonAdicionar() {
        Produto produto;
        ItemDeVenda itemDeVenda = new ItemDeVenda();
        
        if (comboBoxVendaProduto.getSelectionModel().getSelectedItem() != null) {
            produto = (Produto) comboBoxVendaProduto.getSelectionModel().getSelectedItem();
            setEstado(produto.getQuantidade(),produto);    
        if(verificaEstado(produto) == true){            
            if(produto.getQuantidade() >= Integer.parseInt(textFieldVendaItemDeVendaQuantidade.getText())){
                itemDeVenda.setProduto((Produto) comboBoxVendaProduto.getSelectionModel().getSelectedItem());
                itemDeVenda.setQuantidade(Integer.parseInt(textFieldVendaItemDeVendaQuantidade.getText()));
               Boolean bol =  verificarNovamenteEstado(produto,itemDeVenda.getQuantidade());
         if ( bol == true){
                double valortotal = itemDeVenda.getProduto().getPreco() * itemDeVenda.getQuantidade();
                //chamando o metodo do padrão decorator passando em seu parametro o objeto itemDeVenda
                // e o valor total da compra para adicionar 10 reais caso o checkbox de embrulho seja marcado
                double valortotal2 = embrulhar(itemDeVenda, valortotal);
                //chamando o método do padrão chain of responsability em seu parametro e a quantidade de produtos
                //na venda para calcular o desconto
                double valorfinal = descontar(valortotal2,itemDeVenda.getQuantidade());
                itemDeVenda.setValor(valorfinal);
                venda.getItensDeVenda().add(itemDeVenda);

                venda.setValor(venda.getValor() + itemDeVenda.getValor());
                observableListItensDeVenda = FXCollections.observableArrayList(venda.getItensDeVenda());
                tableViewItensDeVenda.setItems(observableListItensDeVenda);
                textFieldVendaValor.setText(String.format("%.2f", venda.getValor()));
         }
         else{
             Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Problema!");
                alert.setContentText("Não existe a quantidade solicitada de produtos disponíveis no estoque");
                alert.show();
         }
            }else{
                 Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Problema!");
                alert.setContentText("Não existem produtos no estoque");
                alert.show();
            }
        }else{
            
        }
    }
    }
//metodo que inicia o padrão decorator 

    public double embrulhar(ComponentItemDeVenda itemDeVenda, double valorTotal) {
        itemDeVenda = new ItemDeVenda(valorTotal);
        //     System.out.println(itemDeVenda.getValor());
        if (checkBoxPresente.isSelected()) {
            //ComponentItemDeVenda item = new ItemDeVenda(valorTotal);
            itemDeVenda = new DecoratorPresente(10.0, itemDeVenda);
            //  item = new DecoratorPresente(10.0,item);
            //    System.out.println(itemDeVenda.getValor());
            return itemDeVenda.getValor();
        } else {
            return itemDeVenda.getValor();
        }

    }
//metodo que inicia o padrão chain of responsability

    public double descontar(double valor, int qtd) {
        Desconto desconto10 = new Desconto10();
        Desconto desconto20 = new Desconto20();
        Desconto desconto30 = new Desconto30();
        Desconto desconto40 = new Desconto40();
        desconto10.setSucessor(desconto20);
        desconto20.setSucessor(desconto30);
        desconto30.setSucessor(desconto40);

        return desconto10.processaDesconto(valor, qtd);
    }

    @FXML
    public void handleButtonConfirmar() {
        if (validarEntradaDeDados()) {
            venda.setCliente((Cliente) comboBoxVendaCliente.getSelectionModel().getSelectedItem());
            venda.setPago(checkBoxVendaPago.isSelected());
            venda.setData(datePickerVendaData.getValue());
            buttonConfirmarClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    public void handleButtonCancelar() {
        getDialogStage().close();
    }

    //Validar entrada de dados para o cadastro
    private boolean validarEntradaDeDados() {
        String errorMessage = "";
        if (comboBoxVendaCliente.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Cliente inválido!\n";
        }
        if (datePickerVendaData.getValue() == null) {
            errorMessage += "Data inválida!\n";
        }
        if (observableListItensDeVenda == null) {
            errorMessage += "Itens de Venda inválidos!\n";
        }
        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Mostrando a mensagem de erro
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro no cadastro");
            alert.setHeaderText("Campos inválidos, por favor, corrija...");
            alert.setContentText(errorMessage);
            alert.show();
            return false;
        }
    }

}
