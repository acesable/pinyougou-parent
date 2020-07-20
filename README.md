# pinyougou-parent
品优购项目实战
测试忽略方式:-->
---------------------------------------------20200718
项目根目录中加入.gitignore文件
文件内容
# idea
.idea/
out/
**/target/
*.iml

# java
*.class
# Log file
*.log
# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar
git add .gitignore
git commit -m "add .gitignore"
git push origin master
---------------------------------------------20200720
手工选择修改和要保留的文件后,commit,push
失败了:把iml文件和target文件夹都上传上去了
