
cmake_minimum_required(VERSION 3.4.1)

add_library(watermark
            SHARED
            src/main/jni/src/watermark.cpp
            src/main/jni/src/CvxText.cpp)
add_library(Speex
            SHARED
            src/main/jni/src/libspeex/Jni_speex.cpp
src/main/jni/src/libspeex/bits.c
src/main/jni/src/libspeex/buffer.c
src/main/jni/src/libspeex/cb_search.c
src/main/jni/src/libspeex/exc_10_16_table.c
src/main/jni/src/libspeex/exc_10_32_table.c
src/main/jni/src/libspeex/exc_20_32_table.c
src/main/jni/src/libspeex/exc_5_256_table.c
src/main/jni/src/libspeex/exc_5_64_table.c
src/main/jni/src/libspeex/exc_8_128_table.c
src/main/jni/src/libspeex/fftwrap.c
src/main/jni/src/libspeex/filterbank.c
src/main/jni/src/libspeex/filters.c
src/main/jni/src/libspeex/gain_table.c
src/main/jni/src/libspeex/gain_table_lbr.c
src/main/jni/src/libspeex/hexc_10_32_table.c
src/main/jni/src/libspeex/hexc_table.c
src/main/jni/src/libspeex/high_lsp_tables.c
src/main/jni/src/libspeex/jitter.c
src/main/jni/src/libspeex/kiss_fft.c
src/main/jni/src/libspeex/kiss_fftr.c
src/main/jni/src/libspeex/lpc.c
src/main/jni/src/libspeex/lsp.c
src/main/jni/src/libspeex/lsp_tables_nb.c
src/main/jni/src/libspeex/ltp.c
src/main/jni/src/libspeex/mdf.c
src/main/jni/src/libspeex/modes.c
src/main/jni/src/libspeex/modes_wb.c
src/main/jni/src/libspeex/nb_celp.c
src/main/jni/src/libspeex/preprocess.c
src/main/jni/src/libspeex/quant_lsp.c
src/main/jni/src/libspeex/resample.c
src/main/jni/src/libspeex/sb_celp.c
src/main/jni/src/libspeex/scal.c
src/main/jni/src/libspeex/smallft.c
src/main/jni/src/libspeex/speex.c
src/main/jni/src/libspeex/speex_callbacks.c
src/main/jni/src/libspeex/speex_header.c
src/main/jni/src/libspeex/stereo.c

src/main/jni/src/libspeex/vbr.c
src/main/jni/src/libspeex/vq.c
src/main/jni/src/libspeex/window.c
)



add_library( # Sets the name of the library.
             WebRtcAudio
             SHARED
             src/main/jni/src/webrtc/web_rtc.cpp
             src/main/jni/src/webrtc/analog_agc.c
             src/main/jni/src/webrtc/complex_bit_reverse.c
             src/main/jni/src/webrtc/complex_fft.c
             src/main/jni/src/webrtc/copy_set_operations.c
             src/main/jni/src/webrtc/digital_agc.c
             src/main/jni/src/webrtc/division_operations.c
             src/main/jni/src/webrtc/dot_product_with_scale.c
             src/main/jni/src/webrtc/downsample_fast.c
             src/main/jni/src/webrtc/energy.c
             src/main/jni/src/webrtc/fft4g.c
             src/main/jni/src/webrtc/get_scaling_square.c
             src/main/jni/src/webrtc/min_max_operations.c

             src/main/jni/src/webrtc/noise_suppression.c
             src/main/jni/src/webrtc/noise_suppression_x.c
             src/main/jni/src/webrtc/ns_core.c
             src/main/jni/src/webrtc/nsx_core.c
             src/main/jni/src/webrtc/nsx_core_c.c
             src/main/jni/src/webrtc/real_fft.c
             src/main/jni/src/webrtc/resample.c
             src/main/jni/src/webrtc/resample_48khz.c
             src/main/jni/src/webrtc/resample_by_2.c
             src/main/jni/src/webrtc/resample_by_2_internal.c
             src/main/jni/src/webrtc/resample_by_2_mips.c
             src/main/jni/src/webrtc/resample_fractional.c
             src/main/jni/src/webrtc/ring_buffer.c
             src/main/jni/src/webrtc/spl_init.c
             src/main/jni/src/webrtc/spl_sqrt.c
             src/main/jni/src/webrtc/spl_sqrt_floor.c
             src/main/jni/src/webrtc/splitting_filter.c
             src/main/jni/src/webrtc/vector_scaling_operations.c
 )

find_library(log-lib log )

add_library(freetype SHARED IMPORTED)
set_target_properties(freetype PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libfreetype.so)

add_library(opencv_java SHARED IMPORTED)
set_target_properties(opencv_java PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java.so)

target_link_libraries(watermark
                      ${log-lib}
                      freetype
                      opencv_java)


target_link_libraries(
                      WebRtcAudio
                      ${log-lib}
                      )

target_link_libraries(Speex
                      ${log-lib}
                      )

include_directories(src/main/jni/include
                    src/main/jni/src/libspeex
                    src/main/jni/include/webrtc
                   )

#link_directories(jniLibs/armeabi-v7a)