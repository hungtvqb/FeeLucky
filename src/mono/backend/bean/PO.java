/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mono.backend.bean;


import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Persistent Object
 * 
 * @author hungtv
 */

public abstract class PO {
    protected String id;
    protected boolean isGenericMapper = false;
    
    public final static String TABLE_NAME_PARAM = "#TABLE_NAME";
    
    public PO(){
    
    }

    @Id		
    @Column(name = "ID")
    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getTableName() {
            Table table = this.getClass().getAnnotation(Table.class);
            String tableName = table.name();

            return tableName;
    }

    @Override
    public String toString() {
            return new StringBuilder(this.getClass().getSimpleName()).append("=")
                            .append(getId()).toString();
    }

    /**
     * hungtv comment XXX ham nay viet kho hieu qua, ai viet day? generic
     * compare PO
     * 
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof PO) || !getClass().equals(o.getClass())) {
                return false;
        }

        if (((PO) o).getId() == null && getId() == null) {
                return super.equals(o);
        }

        return getId() != null ? getId().equals(((PO) o).getId()) : false;
    }

    public boolean isGenericMapper() {
            return isGenericMapper;
    }

    public void setGenericMapper(boolean isGenericMapper) {
            this.isGenericMapper = isGenericMapper;
    }

}