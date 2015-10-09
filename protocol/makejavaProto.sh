# !/bin/bash
protoc --java_out=../projects/GamesServer/src/ ProtoBas.proto ProtoMsg.proto
