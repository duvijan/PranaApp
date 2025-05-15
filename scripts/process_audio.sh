
mkdir -p ../app/src/main/res/raw

echo "Processing Mahan.m4a audio file..."

ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 0 -to 1 -c copy ../app/src/main/res/raw/number_1.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 1 -to 2 -c copy ../app/src/main/res/raw/number_2.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 2 -to 3 -c copy ../app/src/main/res/raw/number_3.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 3 -to 4 -c copy ../app/src/main/res/raw/number_4.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 4 -to 5 -c copy ../app/src/main/res/raw/number_5.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 5 -to 6 -c copy ../app/src/main/res/raw/number_6.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 6 -to 7 -c copy ../app/src/main/res/raw/number_7.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 7 -to 8 -c copy ../app/src/main/res/raw/number_8.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 8 -to 9 -c copy ../app/src/main/res/raw/number_9.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 9 -to 10 -c copy ../app/src/main/res/raw/number_10.mp3

ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 10 -to 12 -c copy ../app/src/main/res/raw/inhale_voice.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 12 -to 14 -c copy ../app/src/main/res/raw/hold_voice.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 14 -to 16 -c copy ../app/src/main/res/raw/exhale_voice.mp3
ffmpeg -i ~/attachments/ca55283d-943d-471d-9d30-cc50271dbfe6/Mahan.m4a -ss 16 -to 18 -c copy ../app/src/main/res/raw/silence_voice.mp3

echo "Audio processing complete."
