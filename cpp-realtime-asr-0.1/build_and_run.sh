set -uex
mkdir -p build
rm -rf build/*
cd build
cmake ..
make -j 2
echo "build success and wait 3s to run"
sleep 3
cd ..
build/realtime_asr "pcm/16k-0.pcm"
