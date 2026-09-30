add_executable(idr-edge
    main.cpp
)

target_include_directories(idr-edge PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/adapters
)

target_link_libraries(idr-edge PRIVATE
    idr-core
    spdlog::spdlog
)

set_target_properties(idr-edge PROPERTIES
    OUTPUT_NAME idr-edge
)
