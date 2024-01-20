package com.artframework.domain.plugin.ui;

import com.artframework.domain.config.GlobalSetting;
import com.artframework.domain.customize.MyPostgreSqlQuery;
import com.artframework.domain.customize.MyPostgreSqlTypeConvert;
import com.artframework.domain.utils.GenerateUtils;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;
import com.baomidou.mybatisplus.generator.keywords.PostgreSqlKeyWordsHandler;
import com.baomidou.mybatisplus.generator.query.SQLQuery;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DomainGeneratorDialog extends JDialog {
    private static final String MY_SQL = "Mysql";
    private static final String POLAR_DB = "Polar DB";
    private static final String PG = "Postgresql";

    private JPanel contentPane;
    private JButton btn_OK;
    private JButton btn_Cancel;
    private JTextField db_url;
    private JTextField db_user;
    private JPasswordField db_password;
    private JComboBox db_type;
    private JTextField t_schema;
    private JLabel l_schema;
    private JCheckBox chk_mapper;
    private JCheckBox chk_domain;
    private JTextField t_domainFile;
    private JButton btn_fileChoose;
    private JTextField t_mapper_package;
    private JTextField t_eneity_package;
    private JTextField t_entity_save;
    private JTextField t_mapper_save;
    private JTextField t_domain_save;
    private JTextField t_domain_package;
    private JTextField t_controller_package;
    private JTextField t_controller_save;
    private JCheckBox chk_controller;


    public DomainGeneratorDialog() {
        this.setTitle("DDD代碼生成器");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btn_OK);

        btn_OK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        btn_Cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        db_type.addItem(MY_SQL);
        db_type.addItem(POLAR_DB);
        db_type.addItem(PG);
        db_type.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    onDbTypeChange(db_type.getSelectedItem().toString());
                }
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        l_schema.setVisible(false);
        t_schema.setVisible(false);

        btn_fileChoose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileChooseDialog();
            }
        });

        chk_domain.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!chk_domain.isSelected()) {
                    setDisable(t_domain_package);
                    setDisable(t_domain_save);
                } else {
                    setEnable(t_domain_package);
                    setEnable(t_domain_save);
                }
            }
        });

        chk_controller.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!chk_controller.isSelected()) {
                    setDisable(t_controller_package);
                    setDisable(t_controller_save);
                } else {
                    setEnable(t_controller_package);
                    setEnable(t_controller_save);
                }
            }
        });

        chk_mapper.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!chk_mapper.isSelected()) {
                    setDisable(t_eneity_package);
                    setDisable(t_entity_save);
                    setDisable(t_mapper_package);
                    setDisable(t_mapper_save);
                } else {
                    setEnable(t_eneity_package);
                    setEnable(t_entity_save);
                    setEnable(t_mapper_package);
                    setEnable(t_mapper_save);
                }
            }
        });
    }

    public void setDisable(JTextField textField) {
        textField.disable();
        textField.setDisabledTextColor(Color.white);
    }

    public void setEnable(JTextField textField) {
        textField.enable();
    }

    private void showFileChooseDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        int result = fileChooser.showDialog(null, "選擇文件");
        if (result == JFileChooser.APPROVE_OPTION) {
            t_domainFile.setText(fileChooser.getSelectedFile().getPath());
        }
    }

    private void onDbTypeChange(String selectedItem) {
        if (selectedItem.equals(MY_SQL)) {
            l_schema.setVisible(false);
            t_schema.setVisible(false);
        } else {
            l_schema.setVisible(true);
            t_schema.setVisible(true);
        }
    }

    private void generate() {
        try {
            DataSourceConfig.Builder builder = new DataSourceConfig
                    .Builder(db_url.getText(), db_user.getText(), String.valueOf(db_password.getPassword()));
            if (Objects.equals(db_type.getSelectedItem(), MY_SQL)) {
                builder.dbQuery(new MySqlQuery())
                        .typeConvert(new MySqlTypeConvert())
                        .keyWordsHandler(new MySqlKeyWordsHandler())
                        .databaseQueryClass(SQLQuery.class);
            } else if (Objects.equals(db_type.getSelectedItem(), POLAR_DB) || Objects.equals(db_type.getSelectedItem(), PG)) {
                builder.dbQuery(new MyPostgreSqlQuery(t_schema.getText()))
                        .schema(t_schema.getText())
                        .typeConvert(new MyPostgreSqlTypeConvert())
                        .keyWordsHandler(new PostgreSqlKeyWordsHandler())
                        .databaseQueryClass(SQLQuery.class);
            }

            DataSourceConfig dataSourceConfig = builder.build();

            GlobalSetting.loadFromDB(dataSourceConfig,
                    new File(t_domainFile.getText()));

            Map<String, String> packageParam=new HashMap<>();
            packageParam.put("tablePackage",t_eneity_package.getText());
            packageParam.put("mapperPackage",t_mapper_package.getText());
            packageParam.put("domainPackage",t_domain_package.getText());
            packageParam.put("controllerPackage",t_controller_package.getText());

            GenerateUtils.generateTables(t_mapper_save.getText()
                    , t_entity_save.getText()
                    , GlobalSetting.INSTANCE.getTableList(), packageParam);
            GenerateUtils.generateDomains(t_domain_save.getText(),
                    t_controller_save.getText(),
                    GlobalSetting.INSTANCE.getDomainList(), packageParam);
        } catch (Exception ex) {
            Messages.showErrorDialog(ex.getMessage(), "錯誤");
        }
    }

    private void onOK() {
        // add your code here
        generate();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
