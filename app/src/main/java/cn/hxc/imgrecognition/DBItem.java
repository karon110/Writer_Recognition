package cn.hxc.imgrecognition;

/**
 * Created by 刘欢 on 2018/4/21.
 */

public class DBItem {
    private String listId;      //案件的ID
    private String txtContent;  //显示的文本内容
    private Boolean isCheck;     //要显示的图片的地址

    //构造函数
    public DBItem(String listId, String txtContent, Boolean isCheck) {
        this.listId = listId;
        this.txtContent = txtContent;
        this.isCheck = isCheck;
    }

    public void setIsCheck(Boolean isCheck) {
        this.isCheck = isCheck;
    }

    public void setTxtContent(String txtContent) {
        this.txtContent = txtContent;
    }

    public void setlistId(String listId) {
        this.listId = listId;
    }

    public String getListId() {
        return listId;
    }

    public Boolean getIsCheck() {return isCheck;}

    public String getTxtContent() {
        return txtContent;
    }
}
