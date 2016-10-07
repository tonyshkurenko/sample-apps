#
#  Copyright 2014-2016 CyberVision, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

cmake_minimum_required(VERSION 2.8.8)

project(C-SDK-sample C)

# Our application name.
set(APP_NAME demo_client)

# Target-independent flags.
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu99 -g -Wall -Wextra")

# Path to the Kaa SDK.
add_subdirectory(libs/kaa)
# Directory containing target support library.
add_subdirectory(targets/${KAA_TARGET})
add_subdirectory(targets/common)

# Demo executable.
if("${KAA_TARGET}" STREQUAL "esp8266")
    add_library(${APP_NAME}_s STATIC src/kaa_demo.c)
    file(WRITE ${CMAKE_BINARY_DIR}/blank.c "")
    add_executable(${APP_NAME} ${CMAKE_BINARY_DIR}/blank.c)
    target_link_libraries(${APP_NAME} ${APP_NAME}_s)
    target_link_libraries(${APP_NAME}_s target_support kaac)
else()
    add_executable(${APP_NAME} src/kaa_demo.c)
    target_link_libraries(${APP_NAME} kaac target_support)
endif()

# For most embedded targets binary stripping is required.
if (${KAA_PRODUCE_BINARY})
    add_custom_command(TARGET ${APP_NAME} POST_BUILD
            COMMAND ${CMAKE_OBJCOPY} -O binary ${APP_NAME} ${APP_NAME}.bin)
endif ()

install(TARGETS ${APP_NAME} DESTINATION bin)