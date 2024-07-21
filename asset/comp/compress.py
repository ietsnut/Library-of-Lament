import struct
from PIL import Image
import numpy as np
import os

def clamp(value, min_value, max_value):
    return max(min_value, min(value, max_value))

def compress_obj_and_png(filename):
    # Verify that the .obj file exists
    obj_filename = filename + '.obj'
    if not os.path.exists(obj_filename):
        raise FileNotFoundError(f"{obj_filename} does not exist.")

    vertices = []
    tex_coords = []
    faces = []

    with open(obj_filename, 'r') as obj_file:
        for line in obj_file:
            if line.startswith('v '):
                parts = line.split()
                x, y, z = [float(coord) for coord in parts[1:4]]
                # Scale and clamp to 0-255
                x, y, z = clamp(int(x * 255), 0, 255), clamp(int(y * 255), 0, 255), clamp(int(z * 255), 0, 255)
                vertices.extend([x, y, z])
            elif line.startswith('vt '):
                parts = line.split()
                u, v = float(parts[1]), float(parts[2])
                # Scale and clamp to 0-255
                u, v = clamp(int(u * 255), 0, 255), clamp(int(v * 255), 0, 255)
                tex_coords.extend([u, v])
            elif line.startswith('f '):
                parts = line.split()
                face = []
                for part in parts[1:]:
                    indices = part.split('/')
                    face.extend([int(indices[0])-1, int(indices[1])-1])  # vertex index and texture coordinate index
                faces.extend(face)

    # Convert data to bytes
    vertex_bytes = bytes(vertices)
    tex_coords_bytes = bytes(tex_coords)
    faces_bytes = bytes(faces)

    # Combine all obj related data into a single binary file
    obj_output_filename = filename + '_obj.bin'
    with open(obj_output_filename, 'wb') as output_file:
        # Write the length of the vertex data
        output_file.write(struct.pack('I', len(vertex_bytes)))
        # Write the vertex data
        output_file.write(vertex_bytes)
        # Write the length of the texture coordinate data
        output_file.write(struct.pack('I', len(tex_coords_bytes)))
        # Write the texture coordinate data
        output_file.write(tex_coords_bytes)
        # Write the length of the face data
        output_file.write(struct.pack('I', len(faces_bytes)))
        # Write the face data
        output_file.write(faces_bytes)

    # Verify that the .png file exists
    png_filename = filename + '.png'
    if not os.path.exists(png_filename):
        raise FileNotFoundError(f"{png_filename} does not exist.")

    # Process PNG file
    image = Image.open(png_filename).convert('RGBA')
    data = np.array(image)

    # Create a new image with 2 bits per pixel (4 colors: black, white, gray, transparent)
    new_data = np.zeros((data.shape[0], data.shape[1]), dtype=np.uint8)

    for i in range(data.shape[0]):
        for j in range(data.shape[1]):
            r, g, b, a = data[i, j]
            if a < 128:
                new_data[i, j] = 0  # Transparent
            elif r < 128 and g < 128 and b < 128:
                new_data[i, j] = 1  # Black
            elif r > 128 and g > 128 and b > 128:
                new_data[i, j] = 2  # White
            else:
                new_data[i, j] = 3  # Gray

    # Save the indexed image to a binary file in PNG format
    indexed_image = Image.fromarray(new_data, mode='P')
    indexed_image.putpalette([
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  # Transparent
        0, 0, 0,  # Black
        255, 255, 255,  # White
        128, 128, 128   # Gray
    ])

    img_output_filename = filename + '_img.bin'
    indexed_image.save(img_output_filename, format='PNG')

def decompress_obj_and_png(filename):
    # Decompress OBJ file
    obj_input_filename = filename + '_obj.bin'
    with open(obj_input_filename, 'rb') as input_file:
        vertex_length = struct.unpack('I', input_file.read(4))[0]
        vertex_bytes = input_file.read(vertex_length)
        vertices = list(vertex_bytes)
        
        tex_coords_length = struct.unpack('I', input_file.read(4))[0]
        tex_coords_bytes = input_file.read(tex_coords_length)
        tex_coords = list(tex_coords_bytes)
        
        faces_length = struct.unpack('I', input_file.read(4))[0]
        faces_bytes = input_file.read(faces_length)
        faces = list(faces_bytes)

    # Reconstruct the .obj file
    obj_output_filename = filename + '_uncompressed.obj'
    with open(obj_output_filename, 'w') as obj_file:
        for i in range(0, len(vertices), 3):
            x, y, z = vertices[i] / 255.0, vertices[i+1] / 255.0, vertices[i+2] / 255.0
            obj_file.write(f"v {x} {y} {z}\n")

        for i in range(0, len(tex_coords), 2):
            u, v = tex_coords[i] / 255.0, tex_coords[i+1] / 255.0
            obj_file.write(f"vt {u} {v}\n")

        for i in range(0, len(faces), 2):
            v_idx, t_idx = faces[i] + 1, faces[i+1] + 1
            obj_file.write(f"f {v_idx}/{t_idx}\n")

    # Decompress PNG file
    img_input_filename = filename + '_img.bin'
    indexed_image = Image.open(img_input_filename).convert('RGBA')

    img_output_filename = filename + '_uncompressed.png'
    indexed_image.save(img_output_filename)

# Example usage
compress_obj_and_png('example')
decompress_obj_and_png('example')
