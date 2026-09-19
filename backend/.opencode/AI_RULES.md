# AI Rules - Must Read First (Portable)

# Ye file project root `src/` se relative hai. Absolute path hardcode mat karo.
# Har AI is file + ../AI_INSTRUCTIONS.md ko pehle padhe.

Key: Always use DataTypeUtility.stringValue(), DataTypeUtility.longValue(), DataTypeUtility.getForeignKeyValue(), etc. Never use raw parsing.
Local DB is loaded from src/main/java/com/backend/plateform/tomcat/MysqlDataSourceService.java:23 loadDatabaseCredentialsFromLocalHost() (dbgym), not from src/main/resources/application.properties.

See ../AI_INSTRUCTIONS.md for full mandatory rules (relative paths only).

