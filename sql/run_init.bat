@echo off
chcp 65001 >nul
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -proot123 mall --default-character-set=utf8mb4 < "C:\Users\Administrator\.qclaw\workspace\mall\sql\init.sql"
echo Exit: %ERRORLEVEL%
