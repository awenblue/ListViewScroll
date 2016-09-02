# ListViewScroll
listview上拉加载下一页

使用方式：

1.Activity实现接口ListViewScrollHelper.NextPage, ListViewScrollHelper.InitAdapter

2.在oncreate函数中初始化
helper = new ListViewScrollHelper(listView, this, this);

3.在initAdapter函数中初始化Adapter

4.在next函数中编写分页请求
