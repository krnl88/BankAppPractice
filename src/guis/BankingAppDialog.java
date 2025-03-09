package guis;

import db_objs.MyJDBC;
import db_objs.Transaction;
import db_objs.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/*
    Displays a custom dialog for our BankingAppGui
 */
public class BankingAppDialog extends JDialog implements ActionListener {
    private User user;
    private BankingAppGui bankingAppGui;
    private JLabel balanceLabel, enterAmountLabel, enterUserLabel;
    private JTextField enterAmountField, enterUserField;
    private JButton actionButton;
    private JPanel pastTransactionPanel;
    private ArrayList<Transaction> pastTransactions;

    public BankingAppDialog(BankingAppGui bankingAppGui, User user){
        // set the size
        setSize(400, 400);

        // add focus to the dialog (can't interact with anything else until dialog is closed)
        setModal(true);

        // loads in teh center of our banking gui
        setLocationRelativeTo(bankingAppGui);

        // when suer closes dialog, it releases its resources that are being used
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // prevents dialog from being resized
        setResizable(false);

        // allows us to manually specify the size and position of each component
        setLayout(null);

        // we will need reference to our gui so that we can update the current balance
        this.bankingAppGui = bankingAppGui;

        // we will need access to the user info to make updates to our db or retrieve data about the user
        this.user = user;
    }

    public void addCurrentBalanceAndAmount(){
        // balance label
        balanceLabel = new JLabel("Balance: $" + user.getCurrentBalance());
        balanceLabel.setBounds(0, 10, getWidth() - 20, 20);
        balanceLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(balanceLabel);

        // enter amount label
        enterAmountLabel = new JLabel("Enter Amount:");
        enterAmountLabel.setBounds(0, 50, getWidth() - 20, 20);
        enterAmountLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        enterAmountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterAmountLabel);

        // enter amount field
        enterAmountField = new JTextField();
        enterAmountField.setBounds(15, 80, getWidth() - 50, 40);
        enterAmountField.setFont(new Font("Dialog", Font.BOLD, 20));
        enterAmountField.setHorizontalAlignment(SwingConstants.RIGHT);
        add(enterAmountField);
    }

    public void addActionButton(String actionButtonType){
        actionButton = new JButton(actionButtonType);
        actionButton.setBounds(15, 300, getWidth() - 50, 40);
        actionButton.setFont(new Font("Dialog", Font.BOLD, 20));
        actionButton.addActionListener(this);
        add(actionButton);
    }

    public void addUserField(){
        // enter user label
        enterUserLabel = new JLabel("Enter User:");
        enterUserLabel.setBounds(0, 160, getWidth() - 20, 20);
        enterUserLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        enterUserLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterUserLabel);

        // enter user field
        enterUserField = new JTextField();
        enterUserField.setBounds(15, 190, getWidth() - 50, 40);
        enterUserField.setFont(new Font("Dialog", Font.BOLD, 20));
        add(enterUserField);
    }

    public void addPastTransactionComponents(){
        //container where we will store each transaction
        pastTransactionPanel = new JPanel();

        pastTransactionPanel.setLayout(new BoxLayout(pastTransactionPanel,  BoxLayout.Y_AXIS));

        pastTransactionPanel.setBounds(0, 20, getWidth() - 15, getHeight()); 

        //add scrollability to the container
        JScrollPane scrollPane = new JScrollPane (pastTransactionPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 20, getWidth() - 15, getHeight()); 
        add(scrollPane);

        //perform db call to retrieve all of the past transaction and store into arraylist
        pastTransactions = MyJDBC.getPastTransaction(user);

        //make a new LinkedHashMap to store transaction by month
        LinkedHashMap <String, ArrayList<Transaction>> groupedTransaction= new LinkedHashMap<>();

        for(int i = 0; i < pastTransactions.size(); i++){ 
        
            // store current transaction
            Transaction pastTransaction = pastTransactions.get(pastTransactions.size()-i-1);
            //get date and time
            LocalDate date = pastTransaction.getTransactionDate().toLocalDate();
            String monthYear = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + date.getYear();

            //if that transaction's month and year is not in the LinkedHashMap, make a new ArrayList to store transactions
            //that occurs during that month and year
            if(!groupedTransaction.containsKey(monthYear)){
                groupedTransaction.put(monthYear, new ArrayList<>());
            }
            //else, just add it to the existing ArrayList
            groupedTransaction.get(monthYear).add(pastTransaction);
        }

            //create a label to show the month and year
            for (Map.Entry<String, ArrayList<Transaction>> entry : groupedTransaction.entrySet()) {
                String month = entry.getKey();
                ArrayList<Transaction> transactions = entry.getValue();

                //a panel to add the month and "show details button"
                JPanel monthPanel = new JPanel();
                monthPanel.setLayout(new BoxLayout(monthPanel, BoxLayout.X_AXIS)); 
                monthPanel.setBounds(50,50,getWidth()-50,getHeight()-50);

                JLabel monthLabel = new JLabel(month);
                monthLabel.setFont(new Font("Dialog", Font.BOLD, 24));
                monthLabel.setBorder(new EmptyBorder(10, 0, 5, 0)); 
                monthPanel.add(monthLabel);

                JButton monthDetail = new JButton("Show Details");
                monthDetail.setBounds(getWidth()-20, 20, 100, 30);
                monthDetail.setFont(new Font("Dialog", Font.BOLD,20));
                monthDetail.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showDetailsPanel(month, transactions);
                    }
                });
                monthPanel.add(monthDetail);

                pastTransactionPanel.add(monthPanel);

            //for each month and year, put the individuals transaction panels
            for (Transaction transaction : transactions) {

                // create a container to store an individual transaction
                JPanel pastTransactionContainer = new JPanel();
                pastTransactionContainer.setLayout(new BorderLayout());

                JLabel transactionTypeLabel = new JLabel(transaction.getTransactionType());
                transactionTypeLabel.setFont(new Font("Dialog", Font.BOLD, 20));

                JLabel transactionAmountLabel = new JLabel(String.valueOf(transaction.getTransactionAmount()));
                transactionAmountLabel.setFont(new Font("Dialog", Font.BOLD, 20));

                JLabel transactionDateLabel = new JLabel(String.valueOf(transaction.getTransactionDate()));
                transactionDateLabel.setFont(new Font("Dialog", Font.BOLD, 20));

                // add to the container
                pastTransactionContainer.add(transactionTypeLabel, BorderLayout.WEST); 
                pastTransactionContainer.add(transactionAmountLabel, BorderLayout.EAST); 
                pastTransactionContainer.add(transactionDateLabel, BorderLayout.SOUTH); 

                pastTransactionContainer.setBackground(Color.WHITE);

                // give a black border to each transaction container
                pastTransactionContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                pastTransactionPanel.add(pastTransactionContainer);

                pastTransactionPanel.setPreferredSize(new java.awt.Dimension(getWidth()-50, getHeight()+ (transactions.size()*60))); 
                pastTransactionPanel.revalidate(); 
                pastTransactionPanel.repaint();
            }
        }

    }

    private void handleTransaction(String transactionType, float amountVal){
        Transaction transaction;

        if(transactionType.equalsIgnoreCase("Deposit")){
            // deposit transaction type
            // add to current balance
            user.setCurrentBalance(user.getCurrentBalance().add(new BigDecimal(amountVal)));

            // create transaction
            // we leave date null because we are going to be using the NOW() in sql which will get the current date
            transaction = new Transaction(user.getId(), transactionType, new BigDecimal(amountVal), null);
        }else{
            // withdraw transaction type
            // subtract from current balance
            user.setCurrentBalance(user.getCurrentBalance().subtract(new BigDecimal(amountVal)));

            // we want to show a negative sign for the amount val when withdrawing
            transaction = new Transaction(user.getId(), transactionType, new BigDecimal(-amountVal), null);
        }

        // update database
        if(MyJDBC.addTransactionToDatabase(transaction) && MyJDBC.updateCurrentBalance(user)){
            // show success dialog
            JOptionPane.showMessageDialog(this, transactionType + " Successfully!");

            // reset the fields
            resetFieldsAndUpdateCurrentBalance();
        }else{
            // show failure dialog
            JOptionPane.showMessageDialog(this, transactionType + " Failed...");
        }

    }

    private void resetFieldsAndUpdateCurrentBalance(){
        // reset fields
        enterAmountField.setText("");

        // only appears when transfer is clicked
        if(enterUserField != null){
            enterUserField.setText("");
        }

        // update current balance on dialog
        balanceLabel.setText("Balance: $" + user.getCurrentBalance());

        // update current balance on main gui
        bankingAppGui.getCurrentBalanceField().setText("$" + user.getCurrentBalance());
    }

    private void handleTransfer(User user, String transferredUser, float amount){
        // attempt to perform transfer
        if(MyJDBC.transfer(user, transferredUser, amount)){
            // show success dialog
            JOptionPane.showMessageDialog(this, "Transfer Success!");
            resetFieldsAndUpdateCurrentBalance();
        }else{
            // show failure dialog
            JOptionPane.showMessageDialog(this, "Transfer Failed...");
        }
    }


    public void showDetailsPanel(String month, ArrayList<Transaction> transactions) {
        //make a new dialog to show financial summaries of that month
        JDialog detailsDialog = new JDialog(this, "Details for " + month, true);
        detailsDialog.setSize(300, 400);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setLayout(new BorderLayout());
    
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        //reset income before calculations
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        
        // Iterate through transactions and get the results of income and expenses
        for (Transaction transaction : transactions) {
            if(transaction.getTransactionType().equalsIgnoreCase("Deposit") ||
                (transaction.getTransactionType().equalsIgnoreCase("transfer") && 
                transaction.getTransactionAmount().compareTo(BigDecimal.ZERO) > 0 )){
                    income = income.add(transaction.getTransactionAmount());
            }

            if(transaction.getTransactionType().equalsIgnoreCase("Withdraw") ||
            (transaction.getTransactionType().equalsIgnoreCase("transfer") && 
            transaction.getTransactionAmount().compareTo(BigDecimal.ZERO) < 0 )){
                expense = expense.subtract(transaction.getTransactionAmount());
            }
        }
        JLabel incomeLabel = new JLabel("Income for this month: $" + income);
        incomeLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        detailsPanel.add(incomeLabel);

        JLabel expenseLabel = new JLabel("Expense for this month: $" + expense);
        expenseLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        detailsPanel.add(expenseLabel);

        detailsDialog.add(detailsPanel, BorderLayout.CENTER);
    
        detailsDialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String buttonPressed = e.getActionCommand();

        // get amount val
        float amountVal = Float.parseFloat(enterAmountField.getText());

        // pressed deposit
        if(buttonPressed.equalsIgnoreCase("Deposit")){
            // we want to handle the deposit transaction
            handleTransaction(buttonPressed, amountVal);
        }else{
            // pressed withdraw or transfer

            // validate input by making sure that withdraw or transfer amount is less than current balance
            // if result is -1 it means that the entered amount is more, 0 means they are equal, and 1 means that
            // the entered amount is less
            int result = user.getCurrentBalance().compareTo(BigDecimal.valueOf(amountVal));
            if(result < 0){
                // display error dialog
                JOptionPane.showMessageDialog(this, "Error: Input value is more than current balance");
                return;
            }

            // check to see if withdraw or transfer was pressed
            if(buttonPressed.equalsIgnoreCase("Withdraw")){
                handleTransaction(buttonPressed, amountVal);
            }else{
                // transfer
                String transferredUser = enterUserField.getText();

                // handle transfer
                handleTransfer(user, transferredUser, amountVal);
            }

        }
    }
}














