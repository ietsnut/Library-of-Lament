__kernel void generate_mipmap(
    __global const uchar* source_pixels,
    __global uchar* target_pixels,
    __global const int* palette,
    const int source_width,
    const int source_height,
    const int target_width,
    const int target_height,
    const int color_map_size
) {
    int x = get_global_id(0);
    int y = get_global_id(1);

    if (x >= target_width || y >= target_height) {
        return;
    }

    float x_ratio = (float)source_width / target_width;
    float y_ratio = (float)source_height / target_height;

    float src_x1 = x * x_ratio;
    float src_y1 = y * y_ratio;
    float src_x2 = (x + 1) * x_ratio;
    float src_y2 = (y + 1) * y_ratio;

    int start_x = (int)floor(src_x1);
    int start_y = (int)floor(src_y1);
    int end_x = min((int)ceil(src_x2), source_width);
    int end_y = min((int)ceil(src_y2), source_height);

    int total_r = 0, total_g = 0, total_b = 0, count = 0;

    for (int sy = start_y; sy < end_y; sy++) {
        for (int sx = start_x; sx < end_x; sx++) {
            int index = source_pixels[sy * source_width + sx];
            if (index < color_map_size) {
                int rgb = palette[index];
                total_r += (rgb >> 16) & 0xFF;
                total_g += (rgb >> 8) & 0xFF;
                total_b += rgb & 0xFF;
                count++;
            }
        }
    }

    uchar best_index = 0;

    if (count == 0) {
        int fallback_y = min(start_y, source_height - 1);
        int fallback_x = min(start_x, source_width - 1);
        best_index = source_pixels[fallback_y * source_width + fallback_x];
    } else {
        int avg_r = total_r / count;
        int avg_g = total_g / count;
        int avg_b = total_b / count;

        int best_dist = INT_MAX;
        for (int i = 0; i < color_map_size; i++) {
            int rgb = palette[i];
            int dr = ((rgb >> 16) & 0xFF) - avg_r;
            int dg = ((rgb >> 8) & 0xFF) - avg_g;
            int db = (rgb & 0xFF) - avg_b;
            int dist = dr * dr + dg * dg + db * db;
            if (dist < best_dist) {
                best_dist = dist;
                best_index = (uchar)i;
            }
        }
    }

    target_pixels[y * target_width + x] = best_index;
}